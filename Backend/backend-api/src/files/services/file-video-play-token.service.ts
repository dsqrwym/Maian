import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { Logger } from 'nestjs-pino';
import { IoRedisService } from '#/cache/redis/ioredis.cache.service.js';
import { REDIS_KEYS } from '#/cache/redis/redis.constants.js';
import { ENV } from '#/config/constants.config.js';
import { FILE_ERROR } from '../constants/files.constants.js';
import { randomUUID } from 'node:crypto';
import type { IVideoStreamQueryDto } from '#/files/dto/video-play-query.dto.js';

/**
 * 视频播放临时 token 的 payload 结构
 * 与主 access token 完全隔离，仅用于视频播放鉴权
 */
export interface VideoPlayTokenPayload {
  /** 产品 ID */
  productId: string;
  /** 文件 ID */
  fileId: string;
  /** 唯一标识*/
  jti: string;
  /** token 类型，固定为 video_play，防止被当作 access token 使用 */
  type: 'video_play';
}

@Injectable()
export class FileVideoPlayTokenService {
  constructor(
    private readonly logger: Logger,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
    private readonly ioRedisService: IoRedisService,
  ) {}

  /**
   * 生成视频播放临时 token
   * @param productId 产品 ID
   * @param fileId 文件 ID
   * @returns 签名后的 JWT token 字符串
   */
  async createPlayToken(productId: string, fileId: string): Promise<string> {
    const ttl = Number(
      this.configService.get<number>(ENV.VIDEO_PLAY_TOKEN_TTL, 30 * 60),
    );
    const jti = randomUUID();

    const payload: VideoPlayTokenPayload = {
      productId,
      fileId,
      jti,
      type: 'video_play',
    };

    // 写入 Redis jti，TTL 与 token 一致
    const redisClient = this.ioRedisService.getClient();
    await redisClient.set(
      REDIS_KEYS.videoPlayJtiKey(jti),
      `${productId}:${fileId}`,
      'EX',
      ttl,
    );

    this.logger.debug(
      { productId, fileId, jti, ttl },
      '[createPlayToken] Video play token issued',
    );

    return await this.jwtService.signAsync(payload, {
      secret: this.configService.getOrThrow<string>(
        ENV.VIDEO_PLAY_TOKEN_SECRET,
      ),
      expiresIn: ttl,
    });
  }

  /**
   * 验证视频播放临时 token
   * 校验签名、exp、type、fileId 匹配、Redis jti 存在性
   * @returns 验证通过后返回 { productId, fileId }
   * @param queryPayload
   */
  async verifyPlayToken(
    queryPayload: IVideoStreamQueryDto,
  ): Promise<{ productId: string; fileId: string }> {
    const { playToken, file_id, product_id } = queryPayload;
    let payload: VideoPlayTokenPayload;

    try {
      payload = await this.jwtService.verifyAsync<VideoPlayTokenPayload>(
        playToken,
        {
          secret: this.configService.getOrThrow<string>(
            ENV.VIDEO_PLAY_TOKEN_SECRET,
          ),
        },
      );
    } catch (err: unknown) {
      // 不在日志中打印完整 playToken
      this.logger.warn(
        { file_id, product_id, errName: err },
        '[verifyPlayToken] Token verification failed',
      );

      if (err instanceof Error && err.name === 'TokenExpiredError') {
        throw new UnauthorizedException(FILE_ERROR.VIDEO_PLAY_TOKEN_EXPIRED);
      }
      throw new UnauthorizedException(FILE_ERROR.VIDEO_PLAY_TOKEN_INVALID);
    }

    // 校验 type
    if (payload.type !== 'video_play') {
      this.logger.warn(
        {
          product_id,
          file_id,
          tokenType: payload.type,
        },
        '[verifyPlayToken] Token type mismatch',
      );
      throw new UnauthorizedException(
        FILE_ERROR.VIDEO_PLAY_TOKEN_TYPE_MISMATCH,
      );
    }

    // 校验 fileId 匹配
    if (payload.fileId !== file_id) {
      this.logger.warn(
        { fileId: file_id, tokenFileId: payload.fileId },
        '[verifyPlayToken] Token fileId mismatch',
      );
      throw new UnauthorizedException(
        FILE_ERROR.VIDEO_PLAY_TOKEN_FILE_MISMATCH,
      );
    }

    if (payload.productId !== product_id) {
      this.logger.warn(
        { productId: product_id, tokenProductId: payload.productId },
        '[verifyPlayToken] Token fileId mismatch',
      );
      throw new UnauthorizedException(
        FILE_ERROR.VIDEO_PLAY_TOKEN_PRODUCT_MISMATCH,
      );
    }

    // 校验 Redis 中 jti 是否存在
    const redisClient = this.ioRedisService.getClient();
    const jtiValue = await redisClient.get(
      REDIS_KEYS.videoPlayJtiKey(payload.jti),
    );

    if (!jtiValue) {
      this.logger.warn(
        { file_id, jti: payload.jti },
        '[verifyPlayToken] Token jti not found in Redis (revoked or expired)',
      );
      throw new UnauthorizedException(FILE_ERROR.VIDEO_PLAY_TOKEN_REVOKED);
    }

    if (jtiValue !== `${payload.productId}:${payload.fileId}`) {
      throw new UnauthorizedException(FILE_ERROR.VIDEO_PLAY_TOKEN_INVALID);
    }

    this.logger.debug(
      {
        productId: payload.productId,
        fileId: payload.fileId,
        jti: payload.jti,
      },
      '[verifyPlayToken] Video play token verified',
    );

    return { productId: payload.productId, fileId: payload.fileId };
  }
}

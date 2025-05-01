import {
    ExceptionFilter,
    Catch,
    ArgumentsHost,
    HttpStatus,
} from '@nestjs/common';
import { FastifyReply } from 'fastify';
import { Prisma } from '../../../prisma/generated/prisma';

@Catch(Prisma.PrismaClientKnownRequestError)
export class PrismaExceptionFilter implements ExceptionFilter {
    catch(exception: Prisma.PrismaClientKnownRequestError, host: ArgumentsHost) {
        const response = host.switchToHttp().getResponse<FastifyReply>();

        let status = HttpStatus.INTERNAL_SERVER_ERROR;
        let message = 'Database error';

        switch (exception.code) {
            case 'P2002':
                status = HttpStatus.CONFLICT;
                message = `Unique constraint failed: ${exception.meta?.target}`;
                break;
            case 'P2025':
                status = HttpStatus.NOT_FOUND;
                message = exception.meta?.cause?.toString() || 'Record not found';
                break;
        }

        response.status(status).send({
            code: status,
            message,
            error: 'Database Error',
        });
    }
}
import { UserStatus } from '#/generated/drizzle/enums.js';

export const USER_REVIEW_REQUIRED = false;

/**
 * 不可登录的用户状态
 */
export const LOGIN_BLOCKED_STATUSES: UserStatus[] = [
  UserStatus.PENDING_VERIFICATION,
  UserStatus.INACTIVE,
  UserStatus.BANNED,
];

/**
 * 可以管理自己的用户状态
 */
export const SELF_MANAGE_ALLOWED_STATUSES: UserStatus[] = [
  UserStatus.ACTIVE,
  UserStatus.PENDING_REVIEW,
  UserStatus.APPROVED,
];

/**
 * 即 产品、类别、批发商 对于零售商的可见性，因为没有实现 PENDING_REVIEW 功能所以允许 ACTIVE 和 APPROVED 的批发商
 */
export const MARKETPLACE_VISIBLE_STATUSES: UserStatus[] = USER_REVIEW_REQUIRED
  ? [UserStatus.APPROVED]
  : [UserStatus.ACTIVE, UserStatus.APPROVED];

export const MARKETPLACE_HIDDEN_STATUSES: UserStatus[] = USER_REVIEW_REQUIRED
  ? [
      UserStatus.PENDING_VERIFICATION,
      UserStatus.ACTIVE,
      UserStatus.PENDING_REVIEW,
      UserStatus.INACTIVE,
      UserStatus.BANNED,
    ]
  : [
      UserStatus.PENDING_VERIFICATION,
      UserStatus.PENDING_REVIEW,
      UserStatus.INACTIVE,
      UserStatus.BANNED,
    ];

export const MARKETPLACE_VISIBLE_STATUS_SET: ReadonlySet<UserStatus> = new Set(
  MARKETPLACE_VISIBLE_STATUSES,
);

export const ORDER_ALLOWED_STATUSES: UserStatus[] = USER_REVIEW_REQUIRED
  ? [UserStatus.APPROVED]
  : [UserStatus.ACTIVE, UserStatus.APPROVED];

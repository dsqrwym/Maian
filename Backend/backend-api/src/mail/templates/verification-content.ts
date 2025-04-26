import { DateFormatService } from '../../common/services/date-format.service';

const supportedLanguages = ['en', 'zh-CH', 'es'] as const; // 以后加新语言，只要在这里加上
type SupportedLang = typeof supportedLanguages[number]; // 这个类型是一个联合类型，表示支持的语言的字符串字面量类型

type VerificationContentTranslation = {
    title: string;
    hello: string;
    thankRegister: string;
    reminderVerification: string;
    content: string;
    repeatReminder: string;
    buttonText: string;
    support: string;
    notReply: string;
};

export const VerificationContent = (dateFormatService: DateFormatService, lang: string = 'en', timezone: string, registerDate: Date) : VerificationContentTranslation =>{
    const lockDate = dateFormatService.addDays(registerDate, 3);
    const deleteDat = dateFormatService.addDays(lockDate, 7);

    const formattedLockDate = dateFormatService.formatDate(lockDate, lang, timezone);
    const formattedDeleteDate = dateFormatService.formatDate(deleteDat, lang, timezone);

    const translations : Record<SupportedLang, VerificationContentTranslation> = {
        "zh-CH": {
            title: '验证邮箱',
            hello: '您好',
            thankRegister: '感谢您注册我们的账户！',
            reminderVerification: '目前您的邮箱尚未完成验证，因此部分功能将受到限制。为了实现正常的功能并保障您的账户安全，请点击下方的“验证按钮”完成邮箱验证。',
            content: ` 根据我们的政策，如果您在
            <strong>${formattedLockDate}</strong>
            前仍未完成验证，您的登录权限将会被暂停，并在随后七天内（即
            <strong>${formattedDeleteDate}</strong>）删除您的账户及所有相关数据。`,
            repeatReminder: '为了实现正常的功能，请点击验证按钮进行验证吧！',
            buttonText: '验证',
            support: '如有任何问题，请随时联系我们的客服团队。',
            notReply: '此邮件由系统自动发送，请勿回复。'
        },
        "en": {
            title: 'Verify Your Email',
            hello: 'Hello',
            thankRegister: 'Thank you for registering your account!',
            reminderVerification: 'Currently, your email has not been verified, so some functions will be restricted. To achieve normal functionality and ensure the security of your account, please click the "Verify" button below to complete the email verification.',
            content: ` According to our policy, if you do not complete the verification before
            <strong>${formattedLockDate}</strong>
            , your login permission will be suspended, and your account and all related data will be deleted within the next seven days (i.e.
            <strong>${formattedDeleteDate}</strong>).`,
            repeatReminder: 'To achieve normal functionality, please click the verification button to verify!',
            buttonText: 'Verify',
            support: 'If you have any questions, please feel free to contact our customer service team.',
            notReply: 'This email is automatically sent by the system, please do not reply.'
        },
        "es": {
            title: '',
            hello: '',
            thankRegister: '',
            reminderVerification: '',
            content: '',
            repeatReminder: '',
            buttonText: '',
            support: '',
            notReply: ''
        }
    }
    return translations[lang] || translations.en;
}
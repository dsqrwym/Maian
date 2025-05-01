const supportedLanguages = ['en', 'zh-CH', 'zh-HK', 'es-ES', 'ca-ES-valencia', 'fr-FR', 'it-IT', 'ru-RU', 'ro-RO', 'pt-PT'] as const; // 以后加新语言，只要在这里加上
type SupportedLang = typeof supportedLanguages[number]; // 这个类型是一个联合类型，表示支持的语言的字符串字面量类型

type VerificationContentTranslation = {
    success: string,
    alreadyVerified: string,
    invalid: string
};

export const getVerificationResponseContent = (lang: string = 'en'): VerificationContentTranslation => {
    const translations: Record<SupportedLang, VerificationContentTranslation> = {
        'zh-CH': {
            success: '您的电子邮件已成功验证。',
            alreadyVerified: '您的电子邮件已被验证。',
            invalid: '无法验证，链接无效或以过期。'
        },
        'zh-HK': {
            success: '您的電子郵件已成功驗證。',
            alreadyVerified: '您的電子郵件已被驗證。',
            invalid: '無法驗證，鏈接無效或以過期。'
        },
        'en': {
            success: 'Your email has been successfully verified.',
            alreadyVerified: 'Your email was already verified.',
            invalid: 'Unable to verify, link is invalid or expired.'
        },
        'es-ES': {
            success: 'Su correo electrónico ha sido verificado con éxito.',
            alreadyVerified: 'Su correo electrónico ya ha sido verificado.',
            invalid: 'No se puede verificar, el enlace es inválido o ha caducado.'
        },
        'ca-ES-valencia': {
            success: 'El seu correu electrònic ha estat verificat amb èxit.',
            alreadyVerified: 'El seu correu electrònic ja ha estat verificat.',
            invalid: 'No es pot verificar, l’enllaç és invàlid o ha caducat.'
        },
        'fr-FR': {
            success: 'Votre adresse e-mail a été vérifiée avec succès.',
            alreadyVerified: 'Votre adresse e-mail a déjà été vérifiée.',
            invalid: 'Impossible de vérifier, le lien est invalide ou a expiré.'
        },
        'it-IT': {
            success: 'La tua email è stata verificata con successo.',
            alreadyVerified: 'La tua email è già stata verificata.',
            invalid: 'Impossibile verificare, il link è non valido o è scaduto.'
        },
        'ru-RU': {
            success: 'Ваш адрес электронной почты был успешно подтвержден.',
            alreadyVerified: 'Ваш адрес электронной почты уже подтвержден.',
            invalid: 'Не удалось подтвердить, ссылка недействительна или истекла.'
        },
        'ro-RO': {
            success: 'Adresa dumneavoastră de e-mail a fost verificată cu succes.',
            alreadyVerified: 'Adresa dumneavoastră de e-mail a fost deja verificată.',
            invalid: 'Nu se poate verifica, linkul este invalid sau a expirat.'
        },
        'pt-PT': {
            success: 'Seu e-mail foi verificado com sucesso.',
            alreadyVerified: 'Seu e-mail já foi verificado.',
            invalid: 'Não é possível verificar, o link é inválido ou expirou.'
        }
    }
    return translations[lang] || translations.en;
}
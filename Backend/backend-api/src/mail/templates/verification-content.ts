import { DateFormatService } from '../../common/services/date-format.service';

const supportedLanguages = ['en', 'zh-CH', 'zh-HK', 'es-ES', 'ca-ES-valencia', 'fr-FR', 'it-IT', 'ru-RU', 'ro-RO', 'pt-PT'] as const; // 以后加新语言，只要在这里加上
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

export const VerificationContent = (dateFormatService: DateFormatService, lang: string = 'en', timezone: string, registerDate: Date): VerificationContentTranslation => {
    const lockDate = dateFormatService.addDays(registerDate, 3);
    const deleteDat = dateFormatService.addDays(lockDate, 7);

    const formattedLockDate = dateFormatService.formatDate(lockDate, lang, timezone);
    const formattedDeleteDate = dateFormatService.formatDate(deleteDat, lang, timezone);

    const translations: Record<SupportedLang, VerificationContentTranslation> = {
        'zh-CH': {
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
        'zh-HK': {
            title: '驗證郵箱',
            hello: '您好',
            thankRegister: '感謝您註冊我們的帳戶！',
            reminderVerification: '目前您的郵箱尚未完成驗證，因此部分功能將受到限制。為了實現正常的功能並保障您的帳戶安全，請點擊下方的“驗證按鈕”完成郵箱驗證。',
            content: `根據我們的政策，如果您在 <strong>${formattedLockDate}</strong> 前仍未完成驗證，您的登入權限將會被暫停，並在隨後七天內（即 <strong>${formattedDeleteDate}</strong>）刪除您的帳戶及所有相關資料。`,
            repeatReminder: '為了實現正常的功能，請點擊驗證按鈕進行驗證吧！',
            buttonText: '驗證',
            support: '如有任何問題，請隨時聯繫我們的客服團隊。',
            notReply: '此郵件由系統自動發送，請勿回覆。'
        },
        'en': {
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
        'es-ES': {
            title: 'Verifique su correo electrónico',
            hello: 'Estimado/a',
            thankRegister: '¡Gracias por registrarse en nuestra cuenta!',
            reminderVerification: 'Actualmente, su correo electrónico no ha sido verificado, por lo que algunas funciones estarán restringidas. Para lograr una funcionalidad normal y garantizar la seguridad de su cuenta, haga clic en el botón "Verificar" a continuación para completar la verificación del correo electrónico.',
            content: `Según nuestra política, si no completa la verificación antes del <strong>${formattedLockDate}</strong>, su permiso de inicio de sesión será suspendido y su cuenta y todos los datos relacionados serán eliminados dentro de los próximos siete días (es decir, <strong>${formattedDeleteDate}</strong>).`,
            repeatReminder: 'Para lograr una funcionalidad normal, ¡haga clic en el botón de verificación para verificar!',
            buttonText: 'Verificar',
            support: 'Si tiene alguna pregunta, no dude en ponerse en contacto con nuestro equipo de atención al cliente.',
            notReply: 'Este correo electrónico es enviado automáticamente por el sistema, por favor no responda.'
        },
        'ca-ES-valencia': {
            title: 'Verifiqueu el vostre correu electrònic',
            hello: 'Benvolgut/da',
            thankRegister: 'Gràcies per registrar el vostre compte!',
            reminderVerification: 'Actualment, el vostre correu electrònic no ha estat verificat, per la qual cosa algunes funcions estaran restringides. Per aconseguir una funcionalitat normal i garantir la seguretat del vostre compte, feu clic al botó "Verificar" a continuació per completar la verificació del correu electrònic.',
            content: `Segons la nostra política, si no completeu la verificació abans del <strong>${formattedLockDate}</strong>, el vostre permís d'inici de sessió serà suspès i el vostre compte i totes les dades relacionades seran eliminats dins dels propers set dies (és a dir, <strong>${formattedDeleteDate}</strong>).`,
            repeatReminder: 'Per aconseguir una funcionalitat normal, feu clic al botó de verificació per verificar!',
            buttonText: 'Verificar',
            support: 'Si teniu alguna pregunta, no dubteu a posar-vos en contacte amb el nostre equip d’atenció al client.',
            notReply: 'Aquest correu electrònic és enviat automàticament pel sistema, si us plau, no respongueu.'
        },
        'fr-FR': {
            title: 'Vérifiez votre adresse e-mail',
            hello: 'Bonjour',
            thankRegister: 'Merci de vous être inscrit à notre compte !',
            reminderVerification: 'Actuellement, votre adresse e-mail n’a pas été vérifiée, certaines fonctions seront donc restreintes. Pour assurer le bon fonctionnement et la sécurité de votre compte, veuillez cliquer sur le bouton "Vérifier" ci-dessous pour compléter la vérification de votre adresse e-mail.',
            content: `Selon notre politique, si vous ne complétez pas la vérification avant le <strong>${formattedLockDate}</strong>, votre autorisation de connexion sera suspendue et votre compte ainsi que toutes les données associées seront supprimés dans les sept jours suivants (c’est-à-dire le <strong>${formattedDeleteDate}</strong>).`,
            repeatReminder: 'Pour assurer le bon fonctionnement, veuillez cliquer sur le bouton de vérification pour vérifier !',
            buttonText: 'Vérifier',
            support: 'Si vous avez des questions, n’hésitez pas à contacter notre équipe du service client.',
            notReply: 'Cet e-mail est envoyé automatiquement par le système, veuillez ne pas répondre.'
        },
        'it-IT': {
            title: 'Verifica il tuo indirizzo email',
            hello: 'Gentile utente',
            thankRegister: 'Grazie per esserti registrato al nostro account!',
            reminderVerification: 'Attualmente, il tuo indirizzo email non è stato verificato, quindi alcune funzionalità saranno limitate. Per garantire il corretto funzionamento e la sicurezza del tuo account, clicca sul pulsante "Verifica" qui sotto per completare la verifica dell’email.',
            content: `Secondo la nostra politica, se non completi la verifica entro il <strong>${formattedLockDate}</strong>, il tuo permesso di accesso sarà sospeso e il tuo account e tutti i dati correlati saranno eliminati entro i successivi sette giorni (cioè entro il <strong>${formattedDeleteDate}</strong>).`,
            repeatReminder: 'Per garantire il corretto funzionamento, clicca sul pulsante di verifica per verificare!',
            buttonText: 'Verifica',
            support: 'Se hai domande, non esitare a contattare il nostro team di assistenza clienti.',
            notReply: 'Questa email è inviata automaticamente dal sistema, per favore non rispondere.'
        },
        'ru-RU': {
            title: 'Подтвердите свой адрес электронной почты',
            hello: 'Здравствуйте',
            thankRegister: 'Спасибо за регистрацию в нашей системе!',
            reminderVerification: 'В настоящее время ваш адрес электронной почты не подтвержден, поэтому некоторые функции будут ограничены. Чтобы обеспечить нормальную работу и безопасность вашей учетной записи, пожалуйста, нажмите кнопку "Подтвердить" ниже для завершения подтверждения электронной почты.',
            content: `Согласно нашей политике, если вы не завершите подтверждение до <strong>${formattedLockDate}</strong>, ваш доступ будет приостановлен, а ваша учетная запись и все связанные данные будут удалены в течение следующих семи дней (то есть до <strong>${formattedDeleteDate}</strong>).`,
            repeatReminder: 'Чтобы обеспечить нормальную работу, пожалуйста, нажмите кнопку подтверждения для верификации!',
            buttonText: 'Подтвердить',
            support: 'Если у вас есть вопросы, пожалуйста, свяжитесь с нашей службой поддержки.',
            notReply: 'Это письмо отправлено автоматически системой, пожалуйста, не отвечайте на него.'
        },
        'ro-RO': {
            title: 'Verificați adresa dvs. de e-mail',
            hello: 'Stimate utilizator',
            thankRegister: 'Vă mulțumim că v-ați înregistrat contul!',
            reminderVerification: 'În prezent, adresa dvs. de e-mail nu a fost verificată, așa că anumite funcționalități vor fi restricționate. Pentru a beneficia de toate funcțiile și pentru a vă proteja contul, vă rugăm să faceți clic pe butonul "Verificare" de mai jos pentru a finaliza verificarea adresei de e-mail.',
            content: `Conform politicii noastre, dacă nu finalizați verificarea până la data de <strong>${formattedLockDate}</strong>, accesul la contul dvs. va fi suspendat, iar contul și toate datele aferente vor fi șterse în următoarele șapte zile (adică până la <strong>${formattedDeleteDate}</strong>).`,
            repeatReminder: 'Pentru a beneficia de funcționalitățile complete, faceți clic pe butonul de verificare!',
            buttonText: 'Verificare',
            support: 'Dacă aveți întrebări, nu ezitați să contactați echipa noastră de asistență pentru clienți.',
            notReply: 'Acest e-mail a fost trimis automat de sistem, vă rugăm să nu răspundeți.'
        },
        'pt-PT': {
            title: 'Verifique o seu endereço de e-mail',
            hello: 'Caro(a) utilizador(a)',
            thankRegister: 'Obrigado por se registar na nossa conta!',
            reminderVerification: 'Atualmente, o seu endereço de e-mail ainda não foi verificado, pelo que algumas funcionalidades estarão limitadas. Para garantir o funcionamento normal e a segurança da sua conta, por favor clique no botão "Verificar" abaixo para completar a verificação do e-mail.',
            content: `De acordo com a nossa política, se não concluir a verificação até <strong>${formattedLockDate}</strong>, o seu acesso será suspenso e a sua conta, bem como todos os dados associados, serão eliminados nos sete dias seguintes (ou seja, até <strong>${formattedDeleteDate}</strong>).`,
            repeatReminder: 'Para garantir o funcionamento normal, clique no botão de verificação para verificar!',
            buttonText: 'Verificar',
            support: 'Se tiver alguma dúvida, não hesite em contactar a nossa equipa de apoio ao cliente.',
            notReply: 'Este e-mail foi enviado automaticamente pelo sistema, por favor não responda.'
        }
    }
    return translations[lang] || translations.en;
}
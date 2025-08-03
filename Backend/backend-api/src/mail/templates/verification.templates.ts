export const getVerificationEmailHtml = ({
  title, // 验证邮箱
  hello, // 您好
  thankRegister, // 感谢您注册我们的账户！
  reminderVerification, // 目前您的邮箱尚未完成验证，因此部分功能将受到限制。为了实现正常的功能并保障您的账户安全，请点击下方的“验证按钮”完成邮箱验证。
  content /* 根据我们的政策，如果您在
    <strong>2025年5月15日</strong>
    前仍未完成验证，您的登录权限将会被暂停，并在随后七天内（即
    <strong>2025年5月22日</strong>）删除您的账户及所有相关数据。*/,
  repeatReminder, // 为了实现正常的功能，请点击验证按钮进行验证吧！
  buttonLink,
  buttonText, // 验证
  support, // 如有任何问题，请随时联系我们的客服团队。
  notReply, // 此邮件由系统自动发送，请勿回复。
}: {
  title: string;
  hello: string;
  thankRegister: string;
  reminderVerification: string;
  content: string;
  repeatReminder: string;
  buttonLink: string;
  buttonText: string;
  support: string;
  notReply: string;
}) => `
<!DOCTYPE html>
<html xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" lang="en">

<head>
	<title></title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0"><!--[if mso]>
<xml><w:WordDocument xmlns:w="urn:schemas-microsoft-com:office:word"><w:DontUseAdvancedTypographyReadingMail/></w:WordDocument>
<o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch><o:AllowPNG/></o:OfficeDocumentSettings></xml>
<![endif]--><!--[if !mso]><!--><!--<![endif]-->
	<style>
		* {
			box-sizing: border-box;
		}

		body {
			margin: 0;
			padding: 0;
		}

		a[x-apple-data-detectors] {
			color: inherit !important;
			text-decoration: inherit !important;
		}

		#MessageViewBody a {
			color: inherit;
			text-decoration: none;
		}

		p {
			line-height: inherit
		}

		.desktop_hide,
		.desktop_hide table {
			mso-hide: all;
			display: none;
			max-height: 0px;
			overflow: hidden;
		}

		.image_block img+div {
			display: none;
		}

		sup,
		sub {
			font-size: 75%;
			line-height: 0;
		}

		@media (max-width:768px) {
			.desktop_hide table.icons-inner {
				display: inline-block !important;
			}

			.icons-inner {
				text-align: center;
			}

			.icons-inner td {
				margin: 0 auto;
			}

			.mobile_hide {
				display: none;
			}

			.row-content {
				width: 100% !important;
			}

			.stack .column {
				width: 100%;
				display: block;
			}

			.mobile_hide {
				min-height: 0;
				max-height: 0;
				max-width: 0;
				overflow: hidden;
				font-size: 0px;
			}

			.desktop_hide,
			.desktop_hide table {
				display: table !important;
				max-height: none !important;
			}
		}
	</style>
	<!--[if mso ]><style>sup, sub { font-size: 100% !important; } sup { mso-text-raise:10% } sub { mso-text-raise:-10% }</style> <![endif]-->
</head>

<body class="body"
	style="background-color: #FFFFFF; margin: 0; padding: 0; -webkit-text-size-adjust: none; text-size-adjust: none;">
	<table class="nl-container" width="100%" border="0" cellpadding="0" cellspacing="0" role="presentation"
		style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #FFFFFF;">
		<tbody>
			<tr>
				<td>
					<table class="row row-1" align="center" width="100%" border="0" cellpadding="0" cellspacing="0"
						role="presentation"
						style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #ecf7fd;">
						<tbody>
							<tr>
								<td>
									<table class="row-content stack" align="center" border="0" cellpadding="0"
										cellspacing="0" role="presentation"
										style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 900px; margin: 0 auto;"
										width="900">
										<tbody>
											<tr>
												<td class="column column-1" width="100%"
													style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; padding-bottom: 5px; padding-top: 5px; vertical-align: top;">
													<table class="heading_block block-1" width="100%" border="0"
														cellpadding="10" cellspacing="0" role="presentation"
														style="mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
														<tr>
															<td class="pad">
																<h1
																	style="margin: 0; color: #049efe; direction: ltr; font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; font-size: 38px; font-weight: 700; letter-spacing: normal; line-height: 1.2; text-align: left; margin-top: 0; margin-bottom: 0; mso-line-height-alt: 46px;">
																	<span class="tinyMce-placeholder"
																		style="word-break: break-word;">${title}</span>
																</h1>
															</td>
														</tr>
													</table>
													<table class="paragraph_block block-2" width="100%" border="0"
														cellpadding="10" cellspacing="0" role="presentation"
														style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;">
														<tr>
															<td class="pad">
																<div
																	style="color:#444a5b;direction:ltr;font-family:Arial, 'Helvetica Neue', Helvetica, sans-serif;font-size:16px;font-weight:400;letter-spacing:0px;line-height:1.2;text-align:left;mso-line-height-alt:19px;">
																	<p style="margin: 0; margin-bottom: 16px;">
																		${hello}，<br>${thankRegister}</p>
																	<p style="margin: 0; margin-bottom: 16px;">
																		${reminderVerification}
																	</p>
																	<p style="margin: 0;">${content}
																	</p>
																</div>
															</td>
														</tr>
													</table>
												</td>
											</tr>
										</tbody>
									</table>
								</td>
							</tr>
						</tbody>
					</table>
					<table class="row row-2" align="center" width="100%" border="0" cellpadding="0" cellspacing="0"
						role="presentation"
						style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #ecf7fd;">
						<tbody>
							<tr>
								<td>
									<table class="row-content stack" align="center" border="0" cellpadding="0"
										cellspacing="0" role="presentation"
										style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-radius: 0; color: #000000; width: 900px; margin: 0 auto;"
										width="900">
										<tbody>
											<tr>
												<td class="column column-1" width="75%"
													style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; padding-bottom: 5px; padding-top: 5px; vertical-align: top;">
													<table class="paragraph_block block-1" width="100%" border="0"
														cellpadding="10" cellspacing="0" role="presentation"
														style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;">
														<tr>
															<td class="pad">
																<div
																	style="color:#444a5b;direction:ltr;font-family:Arial, 'Helvetica Neue', Helvetica, sans-serif;font-size:16px;font-weight:400;letter-spacing:0px;line-height:1.2;text-align:left;mso-line-height-alt:19px;">
																	<p style="margin: 0;">${repeatReminder}</p>
																</div>
															</td>
														</tr>
													</table>
												</td>
												<td class="column column-2" width="25%"
													style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; padding-bottom: 5px; padding-top: 5px; vertical-align: top;">
													<table class="button_block block-1" width="100%" border="0"
														cellpadding="5" cellspacing="0" role="presentation"
														style="mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
														<tr>
															<td class="pad">
																<div class="alignment" align="center"><a href="${buttonLink}"
																		target="_blank"
																		style="color:#ffffff;text-decoration:none;"><!--[if mso]>
<v:roundrect xmlns:v="urn:schemas-microsoft-com:vml" xmlns:w="urn:schemas-microsoft-com:office:word"  href="${buttonLink}"  style="height:25px;width:66px;v-text-anchor:middle;" arcsize="64%" fillcolor="#049efe">
<v:stroke dashstyle="Solid" weight="0px" color="#049efe"/>
<w:anchorlock/>
<v:textbox inset="0px,0px,0px,0px">
<center dir="false" style="color:#ffffff;font-family:sans-serif;font-size:13px">
<![endif]--><span class="button" style="background-color: #049efe; border-bottom: 0px solid transparent; border-left: 0px solid transparent; border-radius: 16px; border-right: 0px solid transparent; border-top: 0px solid transparent; color: #ffffff; display: inline-block; font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; font-size: 13px; font-weight: 400; mso-border-alt: none; padding-bottom: 5px; padding-top: 5px; padding-left: 20px; padding-right: 20px; text-align: center; width: auto; word-break: keep-all; letter-spacing: normal;"><span
																				style="word-break: break-word; line-height: 15.6px;">${buttonText}</span></span><!--[if mso]></center></v:textbox></v:roundrect><![endif]--></a>
																</div>
															</td>
														</tr>
													</table>
												</td>
											</tr>
										</tbody>
									</table>
								</td>
							</tr>
						</tbody>
					</table>
					<table class="row row-3" align="center" width="100%" border="0" cellpadding="0" cellspacing="0"
						role="presentation"
						style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #ecf7fd;">
						<tbody>
							<tr>
								<td>
									<table class="row-content stack" align="center" border="0" cellpadding="0"
										cellspacing="0" role="presentation"
										style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-radius: 0; color: #000000; width: 900px; margin: 0 auto;"
										width="900">
										<tbody>
											<tr>
												<td class="column column-1" width="100%"
													style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; padding-bottom: 5px; padding-top: 5px; vertical-align: top;">
													<table class="paragraph_block block-1" width="100%" border="0"
														cellpadding="10" cellspacing="0" role="presentation"
														style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;">
														<tr>
															<td class="pad">
																<div
																	style="color:#101112;direction:ltr;font-family:Arial, 'Helvetica Neue', Helvetica, sans-serif;font-size:16px;font-weight:400;letter-spacing:0px;line-height:1.2;text-align:left;mso-line-height-alt:19px;">
																	<p style="margin: 0;">${support}</p>
																</div>
															</td>
														</tr>
													</table>
													<table class="icons_block block-2" width="100%" border="0"
														cellpadding="0" cellspacing="0" role="presentation"
														style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; text-align: right; line-height: 0;">
														<tr>
															<td class="pad"
																style="vertical-align: middle; color: #c6c6c6; font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; font-size: 18px; font-weight: 400; text-align: right;">
																<!--[if vml]><table align="right" cellpadding="0" cellspacing="0" role="presentation" style="display:inline-block;padding-left:0px;padding-right:0px;mso-table-lspace: 0pt;mso-table-rspace: 0pt;"><![endif]-->
																<!--[if !vml]><!-->
																<table class="icons-inner"
																	style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; display: inline-block; padding-left: 0px; padding-right: 0px;"
																	cellpadding="0" cellspacing="0" role="presentation">
																	<!--<![endif]-->
																	<tr>
																		<td
																			style="vertical-align: middle; text-align: center; padding-top: 0px; padding-bottom: 0px; padding-left: 0px; padding-right: 0px;">
																			<img class="icon" alt="logo"
																				src="https://1253a1a035.imgdist.com/pub/bfra/1zx13ajb/qw1/qgn/eex/%E6%9C%A8%E7%81%AB%E7%8C%B4%E6%97%A0%E8%83%8C%E6%99%AF.png"
																				height="auto" width="128" align="center"
																				style="display: block; height: auto; margin: 0 auto; border: 0;">
																		</td>
																	</tr>
																	<tr>
																		<td
																			style="font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; font-size: 18px; font-weight: 400; color: #c6c6c6; vertical-align: middle; letter-spacing: undefined; text-align: right; line-height: normal;">
																			Dsqrwym's Project</td>
																	</tr>
																</table>
															</td>
														</tr>
													</table>
												</td>
											</tr>
										</tbody>
									</table>
								</td>
							</tr>
						</tbody>
					</table>
				</td>
			</tr>
		</tbody>
	</table><!-- End -->
    <footer style="text-align: center; font-size: 12px; color: #999;">
        <p>${notReply}</p>
    </footer>
</body>

</html>
`;

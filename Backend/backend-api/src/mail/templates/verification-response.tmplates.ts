export const getVerificationResponseHtml = (message: string): string => {
    return `
      <!DOCTYPE html>
      <html lang="en">
      <head>
          <meta charset="UTF-8">
          <title>Verify Email</title>
          <style>
              body { font-family: Arial; background: #f9f9f9; padding: 50px; text-align: center; }
              .box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); display: inline-block; }
          </style>
      </head>
      <body>
          <div class="box">
              <h2>${message}</h2>
          </div>
      </body>
      </html>
    `;
}
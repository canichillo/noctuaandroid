"C:\Program Files (x86)\Java\jre7\bin\keytool.exe" -V -list -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android >> salida.txt
REM "C:\Program Files (x86)\Java\jre7\bin\keytool.exe" -exportcert -alias androiddebugkey  -keystore "C:\Users\Alberto_2\.android\debug.keystore" | "E:\Programas\OpenSSL-Win32\bin\openssl.exe" sha1 -binary | "E:\Programas\OpenSSL-Win32\bin\openssl.exe" base64
pause
pause
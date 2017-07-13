set JDK_HOME=C:\Program Files\Java\jdk1.8.0_131
set JRE_HOME=C:\Program Files\Java\jre1.8.0_131

"%JDK_HOME%\bin\javapackager.exe" -createjar -v ^
   -outdir build ^
   -outfile DeDopFX ^
   -srcdir out\production ^
   -appclass dedopfx.ui.App ^
   -classpath netcdfAll-4.6.6.jar

"%JDK_HOME%\bin\javapackager.exe" -signjar -v ^
   -outdir build ^
   -keyStore "%JKS_FILE%" ^
   -storePass %JKS_PASSWORD% ^
   -alias tools ^
   -keypass %JKS_TOOLS_PASSWORD% ^
   -srcdir build

"%JDK_HOME%\bin\javapackager.exe" -deploy -v ^
   -native installer ^
   -name "DeDopFX" ^
   -title "DeDopFX Demo" ^
   -description "DeDopFX Demo" ^
   -vendor "Brockmann Consult GmbH" ^
   -outdir dist ^
   -outfile DeDopFX ^
   -srcfiles LICENSE.md ^
   -srcfiles lib\netcdf-license.md ^
   -srcfiles lib\netcdfAll-4.6.6.jar ^
   -srcfiles build\DeDopFX.jar ^
   -appclass dedopfx.ui.App ^
   -BappVersion=0.1 ^
   "-Bruntime=%JRE_HOME%" ^
   -Bicon=src\dedopfx\resources\dedop.ico ^
   -Bidentifier=forman.dedopfx

signtool sign /tr http://timestamp.digicert.com /td sha256 /fd sha256 /f "%CSC_FILE%" /p %CSC_KEY_PASSWORD% dist/bundles/DeDopFX-0.1.exe
signtool sign /tr http://timestamp.digicert.com /td sha256 /fd sha256 /f "%CSC_FILE%" /p %CSC_KEY_PASSWORD% dist/bundles/DeDopFX-0.1.msi

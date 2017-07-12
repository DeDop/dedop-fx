set JDK_HOME=C:\Program Files\Java\jdk1.8.0_131
set JRE_HOME=C:\Program Files\Java\jre1.8.0_131

"%JDK_HOME%\bin\javapackager.exe" -createjar -v ^
   -outdir build ^
   -outfile DeDopFX ^
   -srcdir out\production\dedop-fx ^
   -appclass dedopfx.ui.App ^
   -classpath netcdfAll-4.6.6.jar

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


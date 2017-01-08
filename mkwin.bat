"C:\Program Files\Java\jdk1.8.0_112\bin\javapackager.exe" -createjar -v ^
   -outdir build ^
   -outfile DeDopFX ^
   -srcdir out\production\DeDopFX ^
   -appclass dedopfx.ui.App ^
   -classpath netcdfAll-4.6.6.jar

"C:\Program Files\Java\jdk1.8.0_112\bin\javapackager.exe" -deploy -v ^
   -native installer ^
   -name "DeDopFX" ^
   -title "DeDopFX Demo" ^
   -description "DeDopFX Demo" ^
   -vendor "Norman Fomferra" ^
   -outdir dist ^
   -outfile DeDopFX ^
   -srcfiles LICENSE.md ^
   -srcfiles lib\netcdfAll-4.6.6.jar ^
   -srcfiles build\DeDopFX.jar ^
   -appclass dedopfx.ui.App ^
   -BappVersion=0.1 ^
   "-Bruntime=C:\Program Files\Java\jre1.8.0_112" ^
   -Bicon=src\dedopfx\resources\dedop.ico ^
   -Bidentifier=forman.dedopfx


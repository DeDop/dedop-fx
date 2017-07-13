JDK_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home
JRE_HOME=$JDK_HOME/jre

$JDK_HOME/bin/javapackager -createjar -v \
   -outdir build \
   -outfile DeDopFX \
   -srcdir out/production \
   -appclass dedopfx.ui.App \
   -classpath netcdfAll-4.6.6.jar

$JDK_HOME/bin/javapackager -deploy -v \
   -native installer \
   -name "DeDopFX" \
   -title "DeDopFX Demo" \
   -description "DeDopFX Demo" \
   -vendor "Brockmann Consult GmbH" \
   -outdir dist \
   -outfile DeDopFX \
   -srcfiles LICENSE.md \
   -srcfiles lib/netcdf-license.md \
   -srcfiles lib/netcdfAll-4.6.6.jar \
   -srcfiles build/DeDopFX.jar \
   -appclass dedopfx.ui.App \
   -BappVersion=0.1 \
   -Bruntime=$JRE_HOME \
   -Bicon=src/dedopfx/resources/dedop.icns \
   -Bidentifier=forman.dedopfx

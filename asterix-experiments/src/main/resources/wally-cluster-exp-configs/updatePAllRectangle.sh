rm -rf /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/  
rm -rf /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs-for-rectangle/  
#unzip /data/seok.kim/spatial-index-experiment/files/ingestion-experiment-binary-and-configs.zip -d /data/seok.kim/spatial-index-experiment/
tar -zxvf /data/seok.kim/spatial-index-experiment/files/ingestion-experiment-binary-and-configs-for-rectangle.tar.gz -C /data/seok.kim/spatial-index-experiment/
mv /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs-for-rectangle /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs
unzip /data/seok.kim/spatial-index-experiment/files/asterix-experiments-0.8.7-SNAPSHOT-binary-assembly.zip -d /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/;  
rm -rf /data/seok.kim/spatial-index-experiment/asterix-instance/* 
rm -rf /data/seok.kim/spatial-index-experiment/asterix-instance/.installer
unzip /data/seok.kim/spatial-index-experiment/files/profile-asterix-installer-0.8.7-SNAPSHOT-binary-assembly.zip -d /data/seok.kim/spatial-index-experiment/asterix-instance/;
cp -rf /data/seok.kim/spatial-index-experiment/files/managix-conf.xml /data/seok.kim/spatial-index-experiment/asterix-instance/conf/
cp -rf /data/seok.kim/spatial-index-experiment/files/asterix-configuration.xml /data/seok.kim/spatial-index-experiment/asterix-instance/conf/

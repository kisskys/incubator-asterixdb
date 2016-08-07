cp /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/aql/spatial_3_pidx_load_lake.aql /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/aql/spatial_3_pidx_load.aql;
sleep 1m;

#JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -dsti 0 -dstq 0 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment3.*PIdx.*)|(.*SpatialIndexExperiment3.*Shbtree.*)|(.*SpatialIndexExperiment3.*Rtree.*)' &> run-exp3-lake.log;
#sleep 1m;
#JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -dsti 0 -dstq 0 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment4.*Shbtree.*)|(.*SpatialIndexExperiment4.*Rtree.*)' &> run-exp4-lake.log;
#sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -dsti 0 -dstq 0 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment1.*Shbtree.*)|(.*SpatialIndexExperiment1.*Rtree.*)' &> run-exp1-lake.log;
sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -rcbq 1 -dsti 0 -dstq 1 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment2.*Shbtree.*)|(.*SpatialIndexExperiment2.*Rtree.*)' &> run-exp2-1-lake.log;
sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -rcbq 10 -dsti 0 -dstq 1 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment2.*Shbtree.*)|(.*SpatialIndexExperiment2.*Rtree.*)' &> run-exp2-2-lake.log;
sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -rcbq 100 -dsti 0 -dstq 1 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment2.*Shbtree.*)|(.*SpatialIndexExperiment2.*Rtree.*)' &> run-exp2-3-lake.log;
sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -rcbq 1000 -dsti 0 -dstq 1 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment2.*Shbtree.*)|(.*SpatialIndexExperiment2.*Rtree.*)' &> run-exp2-4-lake.log;
sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -rcbq 1 -dsti 0 -dstq 1 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment5.*Shbtree.*)|(.*SpatialIndexExperiment5.*Rtree.*)' &> run-exp5-1-lake.log;
sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -rcbq 10 -dsti 0 -dstq 1 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment5.*Shbtree.*)|(.*SpatialIndexExperiment5.*Rtree.*)' &> run-exp5-2-lake.log;
sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -rcbq 100 -dsti 0 -dstq 1 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment5.*Shbtree.*)|(.*SpatialIndexExperiment5.*Rtree.*)' &> run-exp5-3-lake.log;
sleep 1m;

JAVA_OPTS="-Djava.util.logging.config.file=/data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/logging.properties -Djava.security.egd=file:/dev/./urandom" ./bin/lsmexprunner -d 3600 -di 1000 -qd 3600 -k /home/seok.kim/.ssh/id_rsa -ler /data/seok.kim/spatial-index-experiment/ingestion-experiment-binary-and-configs/ -mh /data/seok.kim/spatial-index-experiment/asterix-instance -jh /home/seok.kim/jdk1.8.0_77/ -rcbq 1000 -dsti 0 -dstq 1 -lsdist zipfian -lt rectangle -si 1 -of /data/seok.kim/spatial-index-experiment/files/simple-gps-points-120312.txt -qsf /data/seok.kim/spatial-index-experiment/files/QuerySeedTweets10K-from-SyntheticTweets100M-psi27-pid0.adm -ni 10 -oh 130.149.249.51 -op 10100 -rh 130.149.249.51 -rp 19002 -qoh 130.149.249.51 -qop 10101 -u seok.kim -regex '(.*SpatialIndexExperiment5.*Shbtree.*)|(.*SpatialIndexExperiment5.*Rtree.*)' &> run-exp5-4-lake.log;


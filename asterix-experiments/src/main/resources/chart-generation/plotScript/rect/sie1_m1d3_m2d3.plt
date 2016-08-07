#reset
set terminal postscript eps enhanced "Helvetica" 32
set datafile separator ","
#set title "Select query response time \n(in milliseconds)"
#set xlabel "Ingestion rate" offset 0,0.5 
#set ylabel "Inserts per second" offset 1.5,0
set rmargin 1

set boxwidth 0.9 absolute
set key inside right top vertical Right noreverse noenhanced autotitles nobox #samplen 1.0
#set key at 2.1, 450
set key font ",32"
set grid
set style fill pattern border
set style histogram clustered gap 1 title  offset character 0, 0, 0
set style data histogram
#set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics border in scale 0,0 nomirror offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",32"
set xtics   ()

set yrange [0:]
#set ytics 250 font ",32"
set size 1.0,1.0

set output "sie1_m1d3_m2d3_flush_count.eps"
plot "sie1_m1d3_m2d3_flush_count.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 

set yrange [0:110]
set output "sie1_m1d3_m2d3_merge_count.eps"
plot "sie1_m1d3_m2d3_merge_count.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 

set yrange [0:7]
set output "sie1_m1d3_m2d3_writeamp.eps"
plot "sie1_m1d3_m2d3_writeamp.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 

set yrange [0:10000]
set key inside left top vertical Right noreverse noenhanced autotitles nobox #samplen 1.0
set output "sie1_m1d3_m2d3_ips.eps"
plot "sie1_m1d3_m2d3_ips.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 

set yrange [-0.6:0.6]
set key inside right top vertical Right noreverse noenhanced autotitles nobox #samplen 1.0
#set ytics 0.1 
set output "sie1_m1d3_m2d3_change_ratio.eps"
plot "sie1_m1d3_m2d3_change_ratio.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 



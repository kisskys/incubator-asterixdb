reset
set terminal postscript eps enhanced "Helvetica" 32
set output "sie5_overall_insert_ps.eps"
set datafile separator ","

set title "Number of inserts per second"
#set xlabel "Index type"
#set ylabel "Number of inserts per second"
set key off 
set grid
set boxwidth 1.5 relative 
set style fill transparent solid 1.0 noborder 
set style histogram clustered gap 5 title  offset character 0, 0, 0
set style data histograms
set xtics border in scale 0,0 nomirror rotate by -45 offset character 0, 0, 0 autojustify
set xtics norangelimit font ",20"
set xtics   ()
set yrange [0:] 
plot 'sie5_overall_insert_ps.txt' using 2:xtic(1) ti col 

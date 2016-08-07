reset
set terminal postscript eps enhanced "Helvetica" 32
set output "sie5_average_query_response_time.eps"
set datafile separator ","

set title "Average query respone time"
#set xlabel "Index type"
set ylabel "in milliseconds"
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
plot 'sie5_average_query_response_time.txt' using 2:xtic(1) ti col 

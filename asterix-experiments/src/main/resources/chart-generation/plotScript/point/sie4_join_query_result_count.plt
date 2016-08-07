reset
set terminal postscript eps enhanced "Helvetica" 29
set output "sie4_join_query_result_count.eps"
set datafile separator ","

#set title "Join query result count"
set xlabel "Circle radius" offset 0,0.5 
set rmargin 1.0

set boxwidth 0.9 absolute
set grid
set style histogram clustered gap 1 title  offset character 0, 0, 0
set style data histogram
set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",29"
set xtics   ()
set yrange [1:]
set logscale y
#set ytics 300 font ",29"

#enable the following line to see result count from all indexes
#set style fill pattern border
#set key inside left top vertical Right noreverse noenhanced autotitles nobox
#plot "sie4_join_query_result_count.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col

set style fill transparent solid 1.0 noborder
set key off
plot "sie4_join_query_result_count.txt" using 2:xtic(1) ti col





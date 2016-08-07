reset
set terminal postscript eps enhanced "Helvetica" 29
set output "sie3_join_query_response_time_ratio.eps"
set datafile separator ","

#set title "Join query response time \n(in milliseconds)"
set xlabel "Circle radius" offset 0,0.5 
#set ylabel "percentage over rtree" offset 1.5,0
set ylabel "query-response time (% over rtree)" offset 1.5,0
set rmargin 1

set boxwidth 0.9 absolute
set key inside left top vertical Right noreverse noenhanced autotitles nobox
set grid
set style fill pattern border
set style histogram clustered gap 1 title  offset character 0, 0, 0
set style data histogram
set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",29"
set xtics   ()
#set yrange [0:700]
set yrange [0:]
#set ytics 300 font ",29"
#plot "sie3_join_query_response_time.txt" using 2:xtic(1) ti col lc rgb "black", '' u 3 ti col lc rgb "gray", '' u 4 ti col lc rgb "brown", '' u 5 ti col lc rgb "red", '' u 6 ti col lc rgb "blue"
plot "sie3_join_query_response_time.txt" using ($2/$2 * 100):xtic(1) ti col(2) fillstyle pattern 2, '' u ($3/$2 * 100) ti col(3) fillstyle pattern 3




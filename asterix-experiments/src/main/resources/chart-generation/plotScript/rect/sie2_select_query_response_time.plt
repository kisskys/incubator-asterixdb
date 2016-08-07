reset
set terminal postscript eps enhanced "Helvetica" 32
#set output "sie2_select_query_response_time.eps"
set datafile separator ","
#set title "Select query response time \n(in milliseconds)"
set xlabel "Circle radius" offset 0,0.5 
#set ylabel "milliseconds" offset 1.5,0 
set ylabel "query-response time \n(milliseconds)" offset 1.5,-1.0 

set boxwidth 0.9 absolute
#set key inside left top #vertical Right noreverse noenhanced autotitles nobox
set grid
set style fill pattern border
set style histogram clustered gap 1 title  offset character 0, 0, 0
set style data histogram
set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
#set xtics border in scale 0,0 nomirror rotate by -45 offset character 0, 0, 0 autojustify
#set xtics  norangelimit font ",20"
#set xtics   ()
#set ytics 300 font ",32"

set yrange [0:700]
#set key inside left top #vertical Right noreverse noenhanced autotitles nobox
set key at 2.2,690
set output "sie2_select_query_response_time.eps"
#plot "sie2_select_query_response_time.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 

plot "sie2_select_query_response_time.txt" using 2:xtic(1) ti col fillstyle pattern 2, '' u 3 ti col fillstyle pattern 3
#set yrange [0:1000]
#set key inside left top #vertical Right noreverse noenhanced autotitles nobox
#set output "sie2_select_query_response_time1.eps"
#plot "sie2_select_query_response_time1.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 

#set yrange [0:20000]
#set key inside left top #vertical Right noreverse noenhanced autotitles nobox
#set key at -1,25000 #vertical Right noreverse noenhanced autotitles nobox
#set output "sie2_select_query_response_time2.eps"
#plot "sie2_select_query_response_time2.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 


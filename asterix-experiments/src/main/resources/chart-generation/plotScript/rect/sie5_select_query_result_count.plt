reset
set terminal postscript eps enhanced "Helvetica" 32
#set output "sie5_select_query_result_count.eps"
set datafile separator ","
#set title "Select query result count \n(in milliseconds)"
set xlabel "Circle radius" offset 0,0.5 

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
#set yrange [0:]
#set logscale y
#set ytics 300 font ",32"
#plot "sie5_select_query_result_count.txt" using 2:xtic(1) ti col lc rgb "black", '' u 3 ti col lc rgb "gray", '' u 4 ti col lc rgb "brown", '' u 5 ti col lc rgb "red", '' u 6 ti col lc rgb "blue"

#set key out vert
#set yrange [1:]
#set logscale y
#set key inside left top #samplen 1 spacing 0.5 font ",20"
#set output "sie5_select_query_result_count.eps"
#plot "sie5_select_query_result_count.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 


unset logscale y 
#set output "sie5_select_query_result_count1.eps"
#plot "sie5_select_query_result_count1.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 

#set output "sie5_select_query_result_count2.eps"
#plot "sie5_select_query_result_count2.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 

set yrange [1:]
set logscale y
#set style fill transparent solid 1.0 noborder
#set key off
set key inside left top #samplen 1 spacing 0.5 font ",20"
set output "sie5_select_query_result_count.eps"
#plot "sie5_select_query_result_count.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 
#plot "sie5_select_query_result_count.txt" using 2:xtic(1) ti col
plot "sie5_select_query_result_count.txt" using 2:xtic(1) ti col fillstyle pattern 2, '' u 3 ti col fillstyle pattern 3

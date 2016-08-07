reset
set terminal postscript eps enhanced "Helvetica" 29
set output "sie3_select_query_profiled_sidx_cache_miss.eps"
set datafile separator ","
#set title "Select query cache miss in secondary index"
set xlabel "Circle radius" offset 0,0.5 
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
#set yrange [0:]
#set ytics 300 font ",29"
#plot "sie3_select_query_profiled_sidx_cache_miss.txt" using 2:xtic(1) ti col lc rgb "black", '' u 3 ti col lc rgb "gray", '' u 4 ti col lc rgb "brown", '' u 5 ti col lc rgb "red", '' u 6 ti col lc rgb "blue"
plot "sie3_select_query_profiled_sidx_cache_miss.txt" using 2:xtic(1) ti col fillstyle pattern 2, '' u 3 ti col fillstyle pattern 3



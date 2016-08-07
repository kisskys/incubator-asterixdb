reset
set terminal postscript eps enhanced "Helvetica" 29
set output "sie2_query_ps_for_membudget_ratio.eps"
set datafile separator ","
set xlabel "Memory budget" offset 0,0.5 
set ylabel "percentage over rtree" offset 1.5,0
set rmargin 1

set boxwidth 0.9 absolute
set key inside left top horizontal Right noreverse noenhanced autotitles nobox samplen 1 font ",24"
set key at -1.10, 160
set grid
set style fill pattern border
set style histogram clustered gap 1 title  offset character 0, 0, 0
set style data histogram
#set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics border in scale 0,0 nomirror offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",29"
set xtics   ()
set yrange [0:160]
#set ytics 250 font ",29"
#plot "sie3_select_query_response_time.txt" using 2:xtic(1) ti col lc rgb "black", '' u 3 ti col lc rgb "gray", '' u 4 ti col lc rgb "brown", '' u 5 ti col lc rgb "red", '' u 6 ti col lc rgb "blue"
#plot "sie3_select_query_response_time.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 
#plot "sie3_select_query_response_time.txt" using 2 ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 
plot "sie2_query_ps_for_membudget.txt" using ($2/$4*100):xtic(1) ti col(2), '' u ($3/$4*100) ti col(3), '' u ($4/$4*100) ti col(4), '' u ($5/$4*100) ti col, '' u ($6/$4*100) ti col 

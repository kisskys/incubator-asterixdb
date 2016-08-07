reset
set terminal postscript eps enhanced "Helvetica" 29
set output "sie3_index_creation_time.eps"
set datafile separator ","
set ylabel "Minutes" offset 1.5,0
set rmargin 1.0

set boxwidth 0.9 absolute
set grid
set style histogram clustered gap 1 title  offset character 0, 0, 0
set style data histogram
set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",29"
set xtics   ()
set yrange [0:]
#set logscale y
#set ytics 300 font ",29"

#enable the following line to see result count from all indexes
#set style fill pattern border
#set key inside left top vertical Right noreverse noenhanced autotitles nobox
#plot "sie3_join_query_result_count.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col

set style fill transparent solid 1.0 noborder
set key off
#set ytics 10

plot 'sie3_index_creation_time.txt' using 2:xtic(1) ti col 

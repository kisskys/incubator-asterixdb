set terminal postscript eps enhanced "Helvetica" 32

set datafile separator ","
set xlabel "Number of Cells" offset 0,0.2 font "Helvetica, 32"
set key right top
set size 1.1,1.1
set mytics 0
set grid ytics mytics
#set xrange [48:24]
set xrange [40:24]
#set xtics ("2^{48}" 48, "2^{40}" 40, "2^{34}" 34, "2^{32}" 32 , "2^{30}" 30, "2^{28}" 28, "2^{26}" 26, "2^{24}" 24)
set xtics ("2^{40}" 40, "2^{34}" 34, "2^{32}" 32 , "2^{30}" 30, "2^{28}" 28, "2^{26}" 26, "2^{24}" 24)
set yrange [0:]

set ylabel "index size (GB)" offset 1.8
set output "sie3_tune_shbtree_sidx_size.eps"
plot "sie3_tune_shbtree_sidx_size.txt" using 1:2 with linespoints pt 2 ps 3 lt 1 lw 2 linecolor rgb "black" title "shbtree"

set ylabel "index-creation time (minutes)" offset 1.8
set output "sie3_tune_shbtree_index_creation_time.eps"
plot "sie3_tune_shbtree_index_creation_time.txt" using 1:2 with linespoints pt 2 ps 3 lt 1 lw 2 linecolor rgb "black" title "shbtree"

set ylabel "query-response time (milliseconds)" offset 1.8
set output "sie3_tune_shbtree_query_response_time.eps"
#plot "sie3_tune_shbtree_query_response_time.txt" using 1:2 with linespoints pt 2 ps 3 lt 1 lw 2 linecolor rgb "black" title "shbtree"

plot "sie3_tune_shbtree_query_response_time.txt" using 1:2 with linespoints pt 2 ps 3 lt 1 lw 2 linecolor rgb "black" title "r0.00001", \
     "sie3_tune_shbtree_query_response_time.txt" using 1:3 with linespoints pt 12 ps 3 lt 2 lw 2 linecolor rgb "brown" title "r0.0001", \
     "sie3_tune_shbtree_query_response_time.txt" using 1:4 with linespoints pt 9 ps 3 lt 1 lw 2 linecolor rgb "blue" title "r0.001", \
     "sie3_tune_shbtree_query_response_time.txt" using 1:5 with linespoints pt 4 ps 3 lt 4 lw 2 linecolor rgb "#DC143C" title "r0.01", \
     "sie3_tune_shbtree_query_response_time.txt" using 1:6 with linespoints pt 5 ps 3 lt 5 lw 2 linecolor rgb "#4169E1" title "r0.1"

set yrange [0:]
set ylabel "query-response time (% over 2^{30})" offset 1.8
set output "sie3_tune_shbtree_query_response_time_ratio.eps"

plot "sie3_tune_shbtree_query_response_time_ratio.txt" using 1:2 with linespoints pt 2 ps 3 lt 1 lw 2 linecolor rgb "black" title "r0.00001", \
     "sie3_tune_shbtree_query_response_time_ratio.txt" using 1:3 with linespoints pt 12 ps 3 lt 2 lw 2 linecolor rgb "brown" title "r0.0001", \
     "sie3_tune_shbtree_query_response_time_ratio.txt" using 1:4 with linespoints pt 9 ps 3 lt 1 lw 2 linecolor rgb "blue" title "r0.001", \
     "sie3_tune_shbtree_query_response_time_ratio.txt" using 1:5 with linespoints pt 4 ps 3 lt 4 lw 2 linecolor rgb "#DC143C" title "r0.01", \
     "sie3_tune_shbtree_query_response_time_ratio.txt" using 1:6 with linespoints pt 5 ps 3 lt 5 lw 2 linecolor rgb "#4169E1" title "r0.1"

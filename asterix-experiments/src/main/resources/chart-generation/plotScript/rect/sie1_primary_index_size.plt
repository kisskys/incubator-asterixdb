set terminal postscript eps enhanced "Helvetica" 32
set output "sie1_pidx_size.eps"
set datafile separator ","
set xlabel "Number of Nodes" offset 0,0.2 font "Helvetica, 32"
set ylabel "primary-index size (GB)" offset 1.8 font "Helvetica, 32"
set key left top
set size 1.1,1.1
set mytics 0
set grid ytics mytics
set yrange [0:]
#set ytics 1 font ",32"
plot "sie1_primary_index_size.txt" using 1:2 with linespoints pt 2 ps 3 lt 1 lw 2 linecolor rgb "black" title "dhbtree", \
     "sie1_primary_index_size.txt" using 1:3 with linespoints pt 12 ps 3 lt 2 lw 2 linecolor rgb "brown" title "dhvbtree", \
     "sie1_primary_index_size.txt" using 1:4 with linespoints pt 9 ps 3 lt 1 lw 2 linecolor rgb "blue" title "rtree", \
     "sie1_primary_index_size.txt" using 1:5 with linespoints pt 4 ps 3 lt 4 lw 2 linecolor rgb "#DC143C" title "shbtree", \
     "sie1_primary_index_size.txt" using 1:6 with linespoints pt 5 ps 3 lt 5 lw 2 linecolor rgb "#4169E1" title "sif"

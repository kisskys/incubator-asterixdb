set terminal postscript eps enhanced "Helvetica" 32
set output "sie2_accumulated_insert_ps.eps"
set datafile separator ","
set datafile missing "0"
#set title "Inserts per second for every 20 minutes"
set xlabel "Running time in seconds" offset 0,0.2 font "Helvetica, 32"
set ylabel "Inserts per second" offset 1.8 font "Helvetica, 32"
set key top right 
set size 1.1,1.1
#set mytics 0
#set grid ytics mytics
set grid 
#set xtics 20 font ",20"
set xtics 1000
set yrange [0:]
#set ytics 20000 font ",32"
set font ",32"
plot "sie2_accumulated_insert_ps.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "dhbtree", \
     "sie2_accumulated_insert_ps.txt" using 1:3 with lines lt 2 lw 2 linecolor rgb "brown" title "dhvbtree", \
     "sie2_accumulated_insert_ps.txt" using 1:4 with lines lt 3 lw 2 linecolor rgb "blue" title "rtree", \
     "sie2_accumulated_insert_ps.txt" using 1:5 with lines lt 4 lw 2 linecolor rgb "#DC143C" title "shbtree", \
     "sie2_accumulated_insert_ps.txt" using 1:6 with lines lt 5 lw 2 linecolor rgb "#4169E1" title "sif" 


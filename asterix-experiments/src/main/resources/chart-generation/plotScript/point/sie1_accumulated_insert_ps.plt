set terminal postscript eps enhanced "Helvetica" 32
set output "sie1_accumulated_insert_ps.eps"
set datafile separator ","
set datafile missing "0"
#set title "Accumulatd inserts per second for 60 minutes"
set xlabel "Running time in seconds" offset 0,0.2 font "Helvetica, 32"
set ylabel "Inserts per second" offset 1.8 font "Helvetica, 32"
set key top right 
set size 1.1,1.1
#set mytics 0
#set grid ytics mytics
set grid 
#set xtics 20 font ",20"
set xtics 1000
#set yrange [0:200000]
#set ytics 40000 font ",32"
set ytics font ",32"
plot "sie1_accumulated_insert_ps.txt" using 1:2 with lines lt 1 lw 5 linecolor rgb "black" title "dhbtree", \
     "sie1_accumulated_insert_ps.txt" using 1:3 with lines lt 2 lw 5 linecolor rgb "brown" title "dhvbtree", \
     "sie1_accumulated_insert_ps.txt" using 1:4 with lines lt 3 lw 5 linecolor rgb "blue" title "rtree", \
     "sie1_accumulated_insert_ps.txt" using 1:5 with lines lt 4 lw 5 linecolor rgb "#DC143C" title "shbtree", \
     "sie1_accumulated_insert_ps.txt" using 1:6 with lines lt 5 lw 5 linecolor rgb "#4169E1" title "sif" 


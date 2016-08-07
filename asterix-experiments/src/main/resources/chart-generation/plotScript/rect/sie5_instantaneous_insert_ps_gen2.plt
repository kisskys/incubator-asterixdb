set terminal postscript eps enhanced "Helvetica" 32
set datafile separator ","
set title "Number of inserts per second \n for every 10K inserts" 
set key at 600,27000
#set xlabel "every 100000" offset 0,0.2 font "Helvetica, 32"
set ylabel "Number of inserts per second" offset 1.8 font "Helvetica, 32"
set size 1.1,1.1
set mytics 0
set grid ytics mytics
set yrange [0:30000]
#set ytics 30000 font ",32"

set output "sie5_dhbtree_instantaneous_insert_ps_gen2.eps"
plot "sie5_instantaneous_insert_ps_dhbtree_gen2.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "dhbtree"

set output "sie5_dhvbtree_instantaneous_insert_ps_gen2.eps"
plot "sie5_instantaneous_insert_ps_dhvbtree_gen2.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "dhvbtree"

set output "sie5_rtree_instantaneous_insert_ps_gen2.eps"
plot "sie5_instantaneous_insert_ps_rtree_gen2.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "rtree"

set output "sie5_shbtree_instantaneous_insert_ps_gen2.eps"
plot "sie5_instantaneous_insert_ps_shbtree_gen2.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "shbtree"

set output "sie5_sif_instantaneous_insert_ps_gen2.eps"
plot "sie5_instantaneous_insert_ps_sif_gen2.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "sif" 



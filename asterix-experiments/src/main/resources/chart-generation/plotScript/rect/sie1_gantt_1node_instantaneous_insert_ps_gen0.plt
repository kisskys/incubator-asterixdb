set terminal postscript eps enhanced "Helvetica" 32
set datafile separator ","
#set xlabel "every 100000" offset 0,0.2 font "Helvetica, 32"
#set title "Instantaneous number of inserts per second \n for every 100K inserts" 
set ylabel "Inserts per second" offset 1.8 font "Helvetica, 32"
set xlabel "Running time in seconds" offset 1.8 font "Helvetica, 32"
#set key 50,27000
set key top right
set size 1.1,1.1
set mytics 0
set grid ytics mytics
#set xrange [0:100]
set yrange [0:60000]
#set ytics 30000 font ",32"

#set output "sie1_gantt_1node_dhbtree_instantaneous_insert_ps_gen0.eps"
#plot "sie1_gantt_1node_instantaneous_insert_ps_dhbtree_gen0.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "dhbtree"

#set output "sie1_gantt_1node_dhvbtree_instantaneous_insert_ps_gen0.eps"
#plot "sie1_gantt_1node_instantaneous_insert_ps_dhvbtree_gen0.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "dhvbtree"

set output "sie1_gantt_1node_rtree_instantaneous_insert_ps_gen0.eps"
plot "sie1_gantt_1node_instantaneous_insert_ps_rtree_gen0.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "rtree"

set output "sie1_gantt_1node_shbtree_instantaneous_insert_ps_gen0.eps"
plot "sie1_gantt_1node_instantaneous_insert_ps_shbtree_gen0.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "shbtree"

#set output "sie1_gantt_1node_sif_instantaneous_insert_ps_gen0.eps"
#plot "sie1_gantt_1node_instantaneous_insert_ps_sif_gen0.txt" using 1:2 with lines lt 1 lw 2 linecolor rgb "black" title "sif" 



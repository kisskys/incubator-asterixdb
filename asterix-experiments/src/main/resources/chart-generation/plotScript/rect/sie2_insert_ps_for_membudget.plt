reset
set terminal postscript eps enhanced "Helvetica" 29
set output "sie2_insert_ps_for_membudget.eps"
set datafile separator ","
#set title "Select query response time \n(in milliseconds)"
set xlabel "Memory budget" offset 0,0.5 
set ylabel "Inserts per second" offset 1.5,0
set rmargin 1

set boxwidth 0.9 absolute
set key inside left top horizontal Right noreverse noenhanced autotitles nobox samplen 1 font ",24"
set key at -1.50, 240000
set grid
set style fill pattern border
set style histogram clustered gap 1 title  offset character 0, 0, 0
set style data histogram
#set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics border in scale 0,0 nomirror offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",29"
set xtics   ()
set yrange [0:240000]
#set ytics 250 font ",29"
plot "sie2_insert_ps_for_membudget.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 


reset
set terminal postscript eps enhanced "Helvetica" 32 
set output "sie1_writeamp.eps"
set datafile separator ","
#set title "Select query response time \n(in milliseconds)"
#set xlabel "Ingestion rate" offset 0,0.5 
set ylabel "write amplification" offset 1.5,0
set rmargin 1

set boxwidth 0.9 absolute
set key inside right top vertical Right noreverse noenhanced autotitles nobox samplen 1.0
set key at 2.2,6.0
set key font ",32" 
set grid
set style fill pattern border
set style histogram clustered gap 1 title  offset character 0, 0, 0
set style data histogram
#set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics border in scale 0,0 nomirror offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",32"
set xtics   ()
set yrange [0:]
#set ytics 250 font ",29"
set size 0.6,1.0
plot "sie1_writeamp.txt" using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col, '' u 5 ti col, '' u 6 ti col 


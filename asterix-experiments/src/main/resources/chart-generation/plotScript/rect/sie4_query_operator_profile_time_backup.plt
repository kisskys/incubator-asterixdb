reset
set terminal postscript eps enhanced "Helvetica" 29
set datafile separator ","
#set title "Select query response time \n(in milliseconds)"
set ylabel "milliseconds" offset 1.5,0
#set rmargin 1

set boxwidth 0.9 absolute 
#set key inside left top vertical Right noreverse noenhanced autotitles nobox
#set key inside left top vertical Right noreverse invert noenhanced autotitles nobox
set key on outside left top vertical Left noreverse invert noenhanced autotitles nobox font "Helvetica, 20" width -5.0 samplen 2
set grid
#set style fill pattern border
set style fill solid border 
set style histogram rowstacked gap 1 title  offset character 0, 0, 0
set style data histograms
set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",29"
set xtics   ()
set yrange [0:]
#set ytics 250 font ",29"
#plot "sie4_select_query_operator_profiletime.txt" using 2:xtic(1) ti col lc rgb "black", '' u 3 ti col lc rgb "gray", '' u 4 ti col lc rgb "brown", '' u 5 ti col lc rgb "red", '' u 6 ti col lc rgb "blue"
#plot newhistogram "Small Radius", "sie4_select_query_operator_profile_time.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#32CD32", newhistogram "Large Radius", '' u 4:xtic(1) ti col lc rgb "goldenrod", '' u 5 ti col lc rgb "blue", newhistogram "Largest Radius", '' u 6:xtic(1) ti col lc rgb "#DC143C"

# select profiling results
set xlabel "Circle radius 0.00001" offset 0,0.5 
set output "sie4_select_query_operator_profile_time_r0.eps"
plot "sie4_select_query_operator_profile_time_r0.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 6 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "goldenrod", '' u 7 ti col lc rgb "#9ACD32", '' u 8 ti col lc rgb "#1E90FF", '' u 9 ti col lc rgb "#696969", '' u 10 ti col lc rgb "#FF8C00" 

set xlabel "Circle radius 0.0001" offset 0,0.5 
set output "sie4_select_query_operator_profile_time_r1.eps"
plot "sie4_select_query_operator_profile_time_r1.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 6 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "goldenrod", '' u 7 ti col lc rgb "#9ACD32", '' u 8 ti col lc rgb "#1E90FF", '' u 9 ti col lc rgb "#696969", '' u 10 ti col lc rgb "#FF8C00" 

set xlabel "Circle radius 0.001" offset 0,0.5 
set output "sie4_select_query_operator_profile_time_r2.eps"
plot "sie4_select_query_operator_profile_time_r2.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 6 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "goldenrod", '' u 7 ti col lc rgb "#9ACD32", '' u 8 ti col lc rgb "#1E90FF", '' u 9 ti col lc rgb "#696969", '' u 10 ti col lc rgb "#FF8C00" 

set xlabel "Circle radius 0.01" offset 0,0.5 
set output "sie4_select_query_operator_profile_time_r3.eps"
plot "sie4_select_query_operator_profile_time_r3.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 6 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "goldenrod", '' u 7 ti col lc rgb "#9ACD32", '' u 8 ti col lc rgb "#1E90FF", '' u 9 ti col lc rgb "#696969", '' u 10 ti col lc rgb "#FF8C00" 

set xlabel "Circle radius 0.1" offset 0,0.5 
set output "sie4_select_query_operator_profile_time_r4.eps"
plot "sie4_select_query_operator_profile_time_r4.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 6 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "goldenrod", '' u 7 ti col lc rgb "#9ACD32", '' u 8 ti col lc rgb "#1E90FF", '' u 9 ti col lc rgb "#696969", '' u 10 ti col lc rgb "#FF8C00" 

# join profiling results
set xlabel "Circle radius 0.00001" offset 0,0.5 
set output "sie4_join_query_operator_profile_time_r0.eps"
plot "sie4_join_query_operator_profile_time_r0.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 7 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "black", '' u 6 ti col lc rgb "goldenrod", '' u 8 ti col lc rgb "#9ACD32", '' u 9 ti col lc rgb "#1E90FF", '' u 10 ti col lc rgb "#696969", '' u 11 ti col lc rgb "#FF8C00" 

set xlabel "Circle radius 0.0001" offset 0,0.5 
set output "sie4_join_query_operator_profile_time_r1.eps"
plot "sie4_join_query_operator_profile_time_r1.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 7 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "black", '' u 6 ti col lc rgb "goldenrod", '' u 8 ti col lc rgb "#9ACD32", '' u 9 ti col lc rgb "#1E90FF", '' u 10 ti col lc rgb "#696969", '' u 11 ti col lc rgb "#FF8C00"  

set xlabel "Circle radius 0.001" offset 0,0.5 
set output "sie4_join_query_operator_profile_time_r2.eps"
plot "sie4_join_query_operator_profile_time_r2.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 7 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "black", '' u 6 ti col lc rgb "goldenrod", '' u 8 ti col lc rgb "#9ACD32", '' u 9 ti col lc rgb "#1E90FF", '' u 10 ti col lc rgb "#696969", '' u 11 ti col lc rgb "#FF8C00" 

set xlabel "Circle radius 0.01" offset 0,0.5 
set output "sie4_join_query_operator_profile_time_r3.eps"
plot "sie4_join_query_operator_profile_time_r3.txt" using 2:xtic(1) ti col lc rgb "#9932CC", '' u 3 ti col lc rgb "#FF4500", '' u 7 ti col lc rgb "#DC143C", '' u 4 ti col lc rgb "#32CD32", '' u 5 ti col lc rgb "black", '' u 6 ti col lc rgb "goldenrod", '' u 8 ti col lc rgb "#9ACD32", '' u 9 ti col lc rgb "#1E90FF", '' u 10 ti col lc rgb "#696969", '' u 11 ti col lc rgb "#FF8C00"  
#
# Very simple Gantt Chart
# Demonstrate using timecolumn(N,format) to plot time data from 
# multiple columns
#
#$DATA << EOD
#Task start      end
#A     2012-11-01 2012-12-31
#B     2013-01-01 2013-03-14
#C     2013-03-15 2014-04-30
#D     2013-05-01 2013-06-30
#E     2013-07-01 2013-08-31
#F1    2013-09-01 2013-10-31
#F2    2013-09-01 2014-01-17
#F3    2013-09-01 2014-01-30
#F4    2013-09-01 2014-03-31
#G1    2013-11-01 2013-11-27
#G2    2013-11-01 2014-01-17
#L     2013-11-28 2013-12-19
#M     2013-11-28 2014-01-17
##N     2013-12-04 2014-03-02
#O     2013-12-20 2014-01-17
#P     2013-12-20 2014-02-16
#Q     2014-01-05 2014-01-13
#R     2014-01-18 2014-01-30
#S     2014-01-31 2014-03-31
#T     2014-03-01 2014-04-28
#EOD
set terminal postscript eps enhanced "Helvetica" 8 
set xdata time
timeformat = "%Y-%m-%d"
set format x "%b\n'%y"

set yrange [-1:]
OneMonth = strptime("%m","2")
set xtics OneMonth nomirror
set xtics scale 2, 0.5
set mxtics 4
set ytics nomirror
set grid x y
unset key
set title "{/=15 Simple Gantt Chart}\n\n{/:Bold Task start and end times in columns 2 and 3}"
set border 3

T(N) = timecolumn(N,timeformat)

#set style arrow 1 filled size screen 0.02, 15 fixed lt 3 lw 1.5
set style arrow 1 filled size screen 0.02, 15 lt 3 lw 1.5

set output "test.eps"
plot "test.txt" using (T(2)) : ($0) : (T(3)-T(2)) : (0.0) : yticlabel(1) with vector as 1, \
     "test.txt" using (T(2)) : ($0) : 1 with labels right offset 1

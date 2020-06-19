import java.time.Duration;
import java.time.Period;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class Time implements Comparable<Time>
{


    Integer day=0;
    Integer hour=0;
    Integer minute=0;
    Integer second=0;
    Boolean unlimited=false;
    Time()
    {

    }
    Time(String timeString)
    {
        if(timeString.equals("UNLIMITED"))
        {
            unlimited=true;
            second=minute=hour=day=Integer.MAX_VALUE;
        }
        else
        {
            parse(timeString);
        }

    }
    void parse(String string)
    {

        String[] tokens=string.split(":");
        if(string.equals("Negative infinity"))
        {
            day=hour=minute=second=Integer.MIN_VALUE;
        }
        else if(string.contains("-"))
        {

            day=Integer.parseInt(tokens[0].substring(0,string.indexOf("-")));
            hour=Integer.parseInt(tokens[0].substring(string.indexOf("-")+1));
            if(tokens.length>1)
            {
                minute=Integer.parseInt(tokens[1]);
            }
            if(tokens.length>2)
            {
                second=Integer.parseInt(tokens[2]);
            }
        }
        else
        {
            if(tokens.length==1)
            {
                minute=Integer.parseInt(tokens[0]);
            }
            if(tokens.length==2)
            {
                minute=Integer.parseInt(tokens[0]);
                second=Integer.parseInt(tokens[1]);
            }
            if(tokens.length==3)
            {
                hour=Integer.parseInt(tokens[0]);
                minute=Integer.parseInt(tokens[1]);
                second=Integer.parseInt(tokens[2]);
            }
        }

    }

    @Override
    public int compareTo(Time o) {
        if(unlimited)
        {
            return 1;
        }
        if(day>o.day)
        {
            return 1;
        }else if(day<o.day)
        {
            return -1;
        }
        if(hour>o.hour)
        {
            return 1;
        }else if(hour<o.hour)
        {
            return -1;
        }
        if(minute>o.minute)
        {
            return 1;
        }else if(minute<o.minute)
        {
            return -1;
        }
        if(second>o.second)
        {
            return 1;
        }else if(second<o.second)
        {
            return -1;
        }
        return -1;
    }
    public Integer getDay() {
        return day;
    }

    public Integer getHour() {
        return hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public Integer getSecond() {
        return second;
    }

    public Time Minus(Time o)
    {
        if(unlimited)
        {
            return this;
        }else if(o.unlimited)
        {
            return new Time("Negative infinity");
        }
        second-=o.second;
        if(second<0)
        {
            second+=60;
            minute-=1;
        }
        minute-=o.minute;
        if(second<0)
        {
            minute+=60;
            hour-=1;
        }
        hour-=o.hour;
        if(hour<0)
        {
            hour+=24;
            day-=1;
        }
        day-=o.day;
        return this;
    }
    public boolean equals(Time o)
    {
        return (unlimited.equals(true)&&o.unlimited.equals(true))||((day.equals(o.day))&&(hour.equals(o.hour))&&(minute.equals(o.minute))&&(second.equals(o.second)));
    }
    public String toString()
    {
        if(unlimited)
        {
            return "UNLIMITED";
        }
        if(day>0)
        {
            return day.toString()+'-'+String.join(":",hour.toString(),minute.toString(),second.toString());
        }
        else
        {
            return String.join(":",hour.toString(),minute.toString(),second.toString());
        }
    }
    public static void main(String[] args)
    {
//        Set<Time> timeset=new TreeSet<>(new Comparator<Time>() {
//            @Override
//            public int compare(Time o1, Time o2) {
//                return o1.compareTo(o2);
//            }
//
//        });
//        timeset.add(new Time("1-13:24:15"));
//        timeset.add(new Time("2-13:24:15"));
//        timeset.add(new Time("UNLIMITED"));
//        timeset.add(new Time("10-13:24:15"));
//        timeset.add(new Time("9-13:24:15"));
//        timeset.add(new Time("UNLIMITED"));
//        timeset.add(new Time("1-13:24:15"));
//        for(Time t:timeset.stream().sorted().collect(Collectors.toList()))
//        {
//            System.out.println(t);
//        }
        System.out.println("112313");
        System.out.println(new Time("0:20:0").compareTo(new Time("0:16:5")));
        System.out.println((new Time("1-13:24:15")).Minus(new Time("0-20:23:00")));

    }
}
package com.test.sokettestserver;
import java.util.Calendar;

public class WorkTime {

    //화면 킬 시간 변수
    private final int WAKEUP_HOUR = 8;
    private final int WAKEUP_MINIUTE = 30;
    private final int WAKEUP_SECOND = 0;
    private final int WAKEUP_MILISECOND = 0;

    //화면 끌 시간 변수
    private final int GOTOSLEEP_HOUR = 17;
    private final int GOTOSLEEP_MINIUTE = 24;
    private final int GOTOSLEEP_SECOND = 0;
    private final int GOTOSLEEP_MILISECOND = 0;

    // 일과 종료 시간 설정 메소드
    public Calendar finishWorkTime(){
        Calendar finishWorkCalendar = Calendar.getInstance();

        finishWorkCalendar.set(Calendar.HOUR_OF_DAY, GOTOSLEEP_HOUR);
        finishWorkCalendar.set(Calendar.MINUTE, GOTOSLEEP_MINIUTE);
        finishWorkCalendar.set(Calendar.SECOND, GOTOSLEEP_SECOND);
        finishWorkCalendar.set(Calendar.MILLISECOND, GOTOSLEEP_MILISECOND);


        // 일과 종료 시간.
        return finishWorkCalendar;
    }

    // 일과 시작 시간 설정 메소드
    public Calendar startWorkTime() {
        Calendar startWorkCalendar = Calendar.getInstance();

        startWorkCalendar.set(Calendar.HOUR_OF_DAY, WAKEUP_HOUR);
        startWorkCalendar.set(Calendar.MINUTE, WAKEUP_MINIUTE);
        startWorkCalendar.set(Calendar.SECOND, WAKEUP_SECOND);
        startWorkCalendar.set(Calendar.MILLISECOND, WAKEUP_MILISECOND);

        // 일과 시작 시간
        return startWorkCalendar;
    }
}

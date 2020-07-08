public class SimuJob {
    private int nodeNum = 0;
    private Time timeLimit;
    private Time realTime;
    private Boolean typeFlag=false;// True->calibration False->normal
    private Time startTime;
    private String parameter;
    SimuJob(int NodeNum, String TimeLimit, String RealTime, String StartTime, Boolean TypeFlag) {
        nodeNum = NodeNum;
        timeLimit = new Time(TimeLimit);
        realTime = new Time(RealTime);
        startTime= new Time(StartTime);
        typeFlag=TypeFlag;
    }
    public String getRealTime()
    {
        return realTime.toInteger().toString();
    }
    public Integer getStartTime()
    {
        return startTime.toInteger();
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    public Boolean getTypeFlag() {
        return typeFlag;
    }

    public void setTypeFlag(Boolean typeFlag) {
        this.typeFlag = typeFlag;
    }

    public Time getTimeLimit() {
        return timeLimit;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}

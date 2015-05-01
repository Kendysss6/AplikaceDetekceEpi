package com.example.ondra.aplikacedetekceepi;

/**
 * Created by Ondra on 1. 5. 2015.
 */
public class SenzorValue {
    private long timeStamp;
    private float [] values;

    /**
     * Kontruktor
     * @param timeStamp kdy byl zaznam porizen
     * @param values hodnoty X,Y,Z akcelerometru
     */
    public SenzorValue(long timeStamp, float [] values){
        this.timeStamp = timeStamp;
        this.values = values;
    }
    public long getTimeStamp(){
        return timeStamp;
    }

    public float [] getValues(){
        return values;
    }
}

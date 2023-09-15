package org.xiaoxingqi.shengxi.model;

public class TestCounData {
    public String I = "";
    public String E = "";
    public String N = "";
    public String S = "";
    public String T = "";
    public String F = "";
    public String J = "";
    public String P = "";
    public String sex;//男性 女性

    public void setData(String value) {
        if ("I".equalsIgnoreCase(value)) {
            I += value;
        } else if ("E".equalsIgnoreCase(value)) {
            E += value;
        } else if ("N".equalsIgnoreCase(value)) {
            N += value;
        } else if ("S".equalsIgnoreCase(value)) {
            S += value;
        } else if ("T".equalsIgnoreCase(value)) {
            T += value;
        } else if ("F".equalsIgnoreCase(value)) {
            F += value;
        } else if ("J".equalsIgnoreCase(value)) {
            J += value;
        } else if ("P".equalsIgnoreCase(value)) {
            P += value;
        } else {
            sex = value;
        }
    }

    public String outResult() {
        String result = "";
        result += I.length() > E.length() ? "I" : "E";
        result += N.length() > S.length() ? "N" : "S";
        result += T.length() > F.length() ? "T" : "F";
        result += J.length() > P.length() ? "J" : "P";
        return result + " " + sex;
    }

}

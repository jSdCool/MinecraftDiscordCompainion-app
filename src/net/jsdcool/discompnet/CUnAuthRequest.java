package net.jsdcool.discompnet;

public class CUnAuthRequest extends CDataType {


    private static final long serialVersionUID = 1L;
    public String userID,reqnum;

    public CUnAuthRequest(String id,String reqn) {
        userID=id;
        reqnum=reqn;
    }
}

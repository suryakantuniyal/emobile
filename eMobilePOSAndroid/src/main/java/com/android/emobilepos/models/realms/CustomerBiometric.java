package com.android.emobilepos.models.realms;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 5/24/2016.
 */
public class CustomerBiometric extends RealmObject {
    @PrimaryKey
    private String customerId;
    private RealmList<CustomerFid> fids;

//    private String leftFingerOneFid;
//    private String leftFingerTwoFid;
//    private String leftFingerThreeFid;
//    private String leftFingerFourFid;
//    private String rightFingerOneFid;
//    private String rightFingerTwoFid;
//    private String rightFingerThreeFid;
//    private String rightFingerFourFid;


    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

//    public String getLeftFingerOneFid() {
//        return leftFingerOneFid;
//    }
//
//    public void setLeftFingerOneFid(String leftFingerOneFid) {
//        this.leftFingerOneFid = leftFingerOneFid;
//    }
//
//    public String getLeftFingerTwoFid() {
//        return leftFingerTwoFid;
//    }
//
//    public void setLeftFingerTwoFid(String leftFingerTwoFid) {
//        this.leftFingerTwoFid = leftFingerTwoFid;
//    }
//
//    public String getLeftFingerThreeFid() {
//        return leftFingerThreeFid;
//    }
//
//    public void setLeftFingerThreeFid(String leftFingerThreeFid) {
//        this.leftFingerThreeFid = leftFingerThreeFid;
//    }
//
//    public String getLeftFingerFourFid() {
//        return leftFingerFourFid;
//    }
//
//    public void setLeftFingerFourFid(String leftFingerFourFid) {
//        this.leftFingerFourFid = leftFingerFourFid;
//    }
//
//    public String getRightFingerOneFid() {
//        return rightFingerOneFid;
//    }
//
//    public void setRightFingerOneFid(String rightFingerOneFid) {
//        this.rightFingerOneFid = rightFingerOneFid;
//    }
//
//    public String getRightFingerTwoFid() {
//        return rightFingerTwoFid;
//    }
//
//    public void setRightFingerTwoFid(String rightFingerTwoFid) {
//        this.rightFingerTwoFid = rightFingerTwoFid;
//    }
//
//    public String getRightFingerThreeFid() {
//        return rightFingerThreeFid;
//    }
//
//    public void setRightFingerThreeFid(String rightFingerThreeFid) {
//        this.rightFingerThreeFid = rightFingerThreeFid;
//    }
//
//    public String getRightFingerFourFid() {
//        return rightFingerFourFid;
//    }
//
//    public void setRightFingerFourFid(String rightFingerFourFid) {
//        this.rightFingerFourFid = rightFingerFourFid;
//    }

//    public void setFingerFid(ViewCustomerDetails_FA.Finger finger, Fid fid) {
//        String encode = Base64.encodeToString(fid.getData(), Base64.DEFAULT);
//        switch (finger) {
//            case FINGER_ONE_LEFT:
//                setLeftFingerOneFid(encode);
//                break;
//            case FINGER_TWO_LEFT:
//                setLeftFingerTwoFid(encode);
//                break;
//            case FINGER_THREE_LEFT:
//                setLeftFingerThreeFid(encode);
//                break;
//            case FINGER_FOUR_LEFT:
//                setLeftFingerFourFid(encode);
//                break;
//            case FINGER_ONE_RIGHT:
//                setRightFingerOneFid(encode);
//                break;
//            case FINGER_TWO_RIGHT:
//                setRightFingerTwoFid(encode);
//                break;
//            case FINGER_THREE_RIGHT:
//                setRightFingerThreeFid(encode);
//                break;
//            case FINGER_FOUR_RIGHT:
//                setRightFingerFourFid(encode);
//                break;
//        }
//    }

    public RealmList<CustomerFid> getFids() {
        if (null == fids) {
            fids = new RealmList<>();
        }
        return fids;
    }

    public void setFids(RealmList<CustomerFid> fids) {
        this.fids = fids;
    }


}

package drivers.star.utils.sdk31.starprntsdk.functions;


import drivers.star.utils.sdk31.starprntsdk.Communication;

public class ScaleFunctions extends Communication {
    public static StarIoExtScaleDisplayedWeightFunction createRequestDisplayedWeightFunction() {
        return SSCPCBBuilder.createRequestDisplayedWeightFunction();
    }

    public static byte[] createZeroClear() {
        SSCPCBBuilder builder = SSCPCBBuilder.createCommandBuilder();

        builder.appendZeroClear();

        return builder.getCommand();
    }

    public static byte[] createUnitChange() {
        SSCPCBBuilder builder = SSCPCBBuilder.createCommandBuilder();

        builder.appendUnitChange();

        return builder.getCommand();
    }
}

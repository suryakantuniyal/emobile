package drivers;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.CreditCardInfo;
import com.payments.core.AndroidTerminal;
import com.payments.core.CoreAPIListener;
import com.payments.core.CoreDeviceError;
import com.payments.core.CoreError;
import com.payments.core.CoreMessage;
import com.payments.core.CoreRefundResponse;
import com.payments.core.CoreSale;
import com.payments.core.CoreSaleResponse;
import com.payments.core.CoreSettings;
import com.payments.core.CoreSignature;
import com.payments.core.CoreTransactions;
import com.payments.core.DeviceEnum;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;
import protocols.EMSCallBack;

public class EMSWalker implements CoreAPIListener {

	private Activity activity;
	private AndroidTerminal terminal;
	private String TERMINAL_ID = "1007";
	private String SECRET = "secretpass";
	private CreditCardInfo cardManager;
	public static CoreSignature signature;
	private boolean devicePlugged = false;
	public boolean isReadingCard = false;
	public boolean failedProcessing = false;

	public EMSWalker(Activity activity, boolean _devicePlugged) {
		this.activity = activity;
		devicePlugged = _devicePlugged;
		// Looper.prepare();
		terminal = new AndroidTerminal(this);

		ProcessCreditCard_FA.tvStatusMSR.setVisibility(View.VISIBLE);
		ProcessCreditCard_FA.tvStatusMSR.setText("Connecting...");
		new connectWalkerAsync().execute();
		// terminal.init(activity, TERMINAL_ID, SECRET, Currency.EUR);
		//
		// terminal.initDevice(DeviceEnum.WALKER);
		// if(terminal.getDevice().equals(DeviceEnum.WALKER))
		// {
		// activity.runOnUiThread(new Runnable() {
		// public void run() {
		// try
		// {
		// EMSCallBack callBack = (EMSCallBack) activity;
		// callBack.readerConnectedSuccessfully(true);
		//
		// }
		// catch(Exception ex)
		// {
		// ex.printStackTrace();
		// }
		// }
		// });
		// }
	}

	private class connectWalkerAsync extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
//			terminal.init(activity, TERMINAL_ID, SECRET, Currency.EUR);

			terminal.initDevice(DeviceEnum.WALKER);

			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			// if (terminal.getDevice().equals(DeviceEnum.WALKER)) {
			// try {
			// EMSCallBack callBack = (EMSCallBack) activity;
			// callBack.readerConnectedSuccessfully(true);
			//
			// } catch (Exception ex) {
			// ex.printStackTrace();
			// }
			// }
		}
	}

	public void startReading(CreditCardInfo cardInfo) {
		isReadingCard = true;
		if (terminal.getDevice().equals(DeviceEnum.NODEVICE)) {
			CoreSale sale = new CoreSale(cardInfo.dueAmount);
			sale.setCardHolderName(cardInfo.getCardOwnerName());
			sale.setMaskedCardNumber(cardInfo.getCardNumUnencrypted());
			sale.setCardCvv(cardInfo.getCardLast4());
			sale.setCardType(cardInfo.getCardType());
			sale.setExpiryDate(cardInfo.getCardExpMonth() + cardInfo.getCardExpYear());
			sale.setAutoReady(true);
			// sale.addTip(BigDecimal.valueOf(2));
			terminal.processSale(sale);
		} else {
			CoreSale sale = new CoreSale(cardInfo.dueAmount);
			terminal.processSale(sale);
		}

		while (isReadingCard) {
		}
		;
	}

	public boolean deviceConnected() {
		if (terminal.getDevice().equals(DeviceEnum.NODEVICE))
			return false;
		return true;
	}

	public void submitSignature() {
		if (signature.checkSignature()) {
			// signature.signatureText();
			signature.submitSignature();
		}
		//
		// if (signatureCanvas.checkSignature()) {
		// if (type.equals(TransactionType.DEVICE)) {
		// canvasLinearLayout.setVisibility(View.GONE);
		// signatureCanvas.submitSignature();
		// signatureCanvas.clearCanvas(true);
		// } else if (type.equals(TransactionType.TRACK)) {
		// canvasLinearLayout.setVisibility(View.GONE);
		// doTrackSale();
		// signatureCanvas.clearCanvas(false);
		// } else if (type.equals(TransactionType.EMV)) {
		// canvasLinearLayout.setVisibility(View.GONE);
		// doEmvSale();
		// signatureCanvas.clearCanvas(false);
		// }
		// } else {
		// Toast.makeText(MainActivity.this, "Signature cannot be empty.",
		// Toast.LENGTH_SHORT).show();
		// }
	}

	@Override
	public void onError(CoreError coreError, String s) {
		// TODO Auto-generated method stub
		System.out.print(s.toString());
	}

	@Override
	public void onLoginUrlRetrieved(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(CoreMessage msj) {
		// TODO Auto-generated method stub
		System.out.print(msj.toString());

		if (isReadingCard) {
			if (msj.equals(CoreMessage.DEVICE_NOT_CONNECTED)) {
				failedProcessing = true;
				isReadingCard = false;
			} else if (msj.equals(CoreMessage.CARD_ERROR))
				isReadingCard = false;
		}
		// if(msj.equals(CoreMessage.CARD_ERROR))
		// isReadingCard = false;

	}

	@Override
	public void onRefundResponse(CoreRefundResponse arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSaleResponse(CoreSaleResponse response) {
		// TODO Auto-generated method stub
		isReadingCard = false;
		try {
			EMSCallBack callBack = (EMSCallBack) activity;
			cardManager = new CreditCardInfo();
			cardManager.setCardOwnerName(response.getCardHolderName());
			cardManager.setCardType(response.getCardType());
			cardManager.authcode = response.getApprovalCode();
			cardManager.transid = response.getUniqueRef();
			cardManager.setWasSwiped(false);
			cardManager.setCardLast4(response.getCardNumber().substring(response.getCardNumber().length() - 4));
			callBack.cardWasReadSuccessfully(true, cardManager);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onSettingsRetrieved(CoreSettings arg0) {
		// TODO Auto-generated method stub
		if (devicePlugged) {
			terminal.initDevice(DeviceEnum.WALKER);
			try {
				EMSCallBack callBack = (EMSCallBack) activity;
				callBack.readerConnectedSuccessfully(true);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else
			terminal.initDevice(DeviceEnum.NODEVICE);

	}

	@Override
	public void onSignatureRequired(CoreSignature _signature) {
		// TODO Auto-generated method stub
		signature = _signature;
		try {
			EMSCallBack callBack = (EMSCallBack) activity;
			callBack.startSignature();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onTransactionListResponse(CoreTransactions arg0) {
		// TODO Auto-generated method stub
		System.out.print(arg0.toString());
	}

	@Override
	public void onDeviceConnected(DeviceEnum deviceEnum, HashMap<String, String> arg1) {
		Toast.makeText(this.activity, deviceEnum.name() + " connected", Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onDeviceDisconnected(DeviceEnum deviceEnum) {
		Toast.makeText(this.activity, deviceEnum.name() + " disconnected", Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onDeviceError(CoreDeviceError arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSelectApplication(ArrayList<String> arg0) {
		// TODO Auto-generated method stub
		
	}

}

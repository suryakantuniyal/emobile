package com.honeywell.decodeconfigcommon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.util.Log;
import com.honeywell.decodemanager.barcode.SymbologyConfig;
import java.util.ArrayList;

class SymbologyConfigDoc
  implements SymbologyConfigDocIf
{
  private static final int M_mode = 3;
  private String mPrefs = null;
  private String mPrefsCopy = null;
  private ArrayList<SymbologySetting> mSymbologySettingMap;
  private ArrayList<Integer> m_ArrayAllActiveSymId = null;
  private ArrayList<Integer> m_ArrayActiveCommonSymId = null;
  private ArrayList<Integer> m_ArrayActivePostalSymId = null;

  private ArrayList<SymbologyCommonProperty> m_arrySymProperty = null;

  private int mOcrMode = -1;
  private int mOcrLength = -1;
  private boolean mbOcrSupport = false;

  private IDecoderService m_Decoderservers = null;
  private Context mOtherContex = null;
  private Context m_Context = null;

  private PostalCodeSymbology mPostSymbology = null;
  private ParseSymbologyProperty mParseSymbologyProperty;
  private final String _MIN_FROM = "_Min_From";
  private final String _MIN_TO = "_Min_To";
  private final String _MIN_LENGTH = "_Min_Length";
  private final String _MAX_FROM = "_Max_From";
  private final String _MAX_TO = "_Max_To";
  private final String _MAX_LENGTH = "_Max_Length";
  private static final String TAG = "DecodeManager";
  private static final String OCRMODE = "OCRMode";
  private static final String OCRLENGTH = "OCRLength";
  private static final String OCRBSUPPORT = "OCRBSupport";
  private static final String PROPERTYLIST = "PropertyList";
  private static final String PROPERTYENABLELIST = "PropertyEnableList";
  private static final String AVAILABLE = " available ";

  public SymbologyConfigDoc(String strSourceConfigName, Context context)
  {
    this.mSymbologySettingMap = new ArrayList();

    this.m_ArrayAllActiveSymId = new ArrayList();
    this.m_ArrayActiveCommonSymId = new ArrayList();
    this.m_ArrayActivePostalSymId = new ArrayList();

    this.mParseSymbologyProperty = new ParseSymbologyProperty();
    this.mPostSymbology = new PostalCodeSymbology();

    this.m_arrySymProperty = new ArrayList();

    this.mPrefs = strSourceConfigName;
    this.mPrefsCopy = this.mPrefs;
    this.m_Context = context;
    try
    {
      this.mOtherContex = this.m_Context.createPackageContext("com.honeywell.decodeconfig", 2);
    }
    catch (PackageManager.NameNotFoundException e)
    {
      e.printStackTrace();
    }
  }

  public void loadSymbologyConfig(IDecoderService Decoderservers, boolean bDefaultSymbologySetting)
  {
    this.m_Decoderservers = Decoderservers;

    if (bDefaultSymbologySetting) {
      this.mPrefs = "DEFAULT";
      loadSharePreferenceSymbologyConfig(this.m_Decoderservers);
      SaveSymbologyConfig();
    } else {
      loadSharePreferenceSymbologyConfig(this.m_Decoderservers);
    }
  }

  public void loadSymbologyConfigwithProcess(IDecoderService Decoderservers, String processname, boolean bDefaultSymbologySetting)
  {
    this.m_Decoderservers = Decoderservers;

    if (bDefaultSymbologySetting) {
      this.mPrefs = "DEFAULT";
      loadSharePreferenceSymbologyConfig(this.m_Decoderservers);
      SaveSymbologyConfigWithProcess(processname);
    } else {
      loadSharePreferenceSymbologyConfig(this.m_Decoderservers);
    }
  }

  public void loadSharePreferenceSymbologyConfig(IDecoderService Decoderservers)
  {
    if (this.m_Decoderservers != null) {
      SharedPreferences Prefs = this.mOtherContex.getSharedPreferences(this.mPrefs, 3);

      if (this.mSymbologySettingMap != null) {
        this.mSymbologySettingMap.clear();
      }

      LoadSymbologyConfigFromSharePreference(Prefs, 25, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 0, 1, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 3750, 1, 1, 3750, 3750);

      LoadSymbologyConfigFromSharePreference(Prefs, 23, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 24, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 1, 536870927, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 2, 60, 2, 2, 60, 60);

      LoadSymbologyConfigFromSharePreference(Prefs, 2, 3, 2, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 80, 4, 1, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 34, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 4, 47, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 0, 48, 2, 0, 48, 48);

      LoadSymbologyConfigFromSharePreference(Prefs, 6, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 0, 80, 0, 0, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 3, 1, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 0, 80, 0, 0, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 38, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 4, 80, 4, 4, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 27, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 0, 2048, 0, 0, 2048, 2048);

      LoadSymbologyConfigFromSharePreference(Prefs, 7, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 480, 1, 1, 480, 480);

      LoadSymbologyConfigFromSharePreference(Prefs, 8, 1, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 1500, 1, 1, 1500, 1500);

      LoadSymbologyConfigFromSharePreference(Prefs, 9, 1925, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 10, 1925, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 18, 58720257, 58720256, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 80, 1, 1, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 47, 1, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 0, 80, 0, 0, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 48, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 6000, 1, 1, 6000, 6000);

      LoadSymbologyConfigFromSharePreference(Prefs, 45, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 22, 1, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 11, 7, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 2, 80, 4, 2, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 28, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 30, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 39, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 4, 48, 4, 4, 48, 48);

      LoadSymbologyConfigFromSharePreference(Prefs, 36, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 4, 80, 4, 4, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 12, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 150, 1, 1, 150, 150);

      LoadSymbologyConfigFromSharePreference(Prefs, 13, 1, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 2750, 1, 1, 2750, 2750);

      LoadSymbologyConfigFromSharePreference(Prefs, 31, 5, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 4, 48, 4, 4, 48, 48);

      LoadSymbologyConfigFromSharePreference(Prefs, 15, 1, 1, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 2750, 1, 1, 2750, 2750);

      LoadSymbologyConfigFromSharePreference(Prefs, 29, 5, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 16, 5, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 17, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 3500, 1, 1, 3500, 3500);

      LoadSymbologyConfigFromSharePreference(Prefs, 26, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 4, 80, 4, 4, 80, 80);

      LoadSymbologyConfigFromSharePreference(Prefs, 35, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 4, 48, 4, 4, 48, 48);

      LoadSymbologyConfigFromSharePreference(Prefs, 33, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 40, 67108865, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.HAVE_MINMAX_PROPERTY, 1, 60, 1, 1, 60, 60);

      LoadSymbologyConfigFromSharePreference(Prefs, 32, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 19, 1989, 65, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 43, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 20, 4037, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadSymbologyConfigFromSharePreference(Prefs, 44, 1, 0, SymbologyActivityProperty.active, HaveMinMaxProperty.NO_MINMAX_PROPERTY, 0, 0, 0, 0, 0, 0);

      LoadOCRConfig(Prefs);
      LoadAllSymbologyId();

      writeSymbologyConfigure(this.m_Decoderservers);
    } else {
      Log.e("DecodeManager", "loadSharePreferenceSymbologyConfig-- Can't write symbology configuration to decode");
    }
  }

  private void LoadOCRConfig(SharedPreferences Prefs)
  {
    this.mOcrMode = Prefs.getInt("OCRMode", 0);
    this.mOcrLength = Prefs.getInt("OCRLength", 8);
    this.mbOcrSupport = Prefs.getBoolean("OCRBSupport", true);
  }

  public int getOcrMode() {
    return this.mOcrMode;
  }

  public void setOcrMode(int OcrMode) {
    this.mOcrMode = OcrMode;
  }

  public int getOcrLength() {
    return this.mOcrLength;
  }

  public void setOcrLength(int length) {
    this.mOcrLength = length;
  }

  public boolean getbOcrSupport() {
    return this.mbOcrSupport;
  }

  public void SaveSymbologyConfig()
  {
    if (this.m_Decoderservers != null) {
      doSaveSymbologyConfig();
      writeSymbologyConfigure(this.m_Decoderservers);
    } else {
      Log.e("DecodeManager", "Can't write symbology configuration to decode");
    }
  }

  public void SaveSymbologyConfigWithProcess(String processname)
  {
    if (this.m_Decoderservers != null) {
      doSaveSymbologyConfig();
      writeSymbologyConfigureWithProcess(this.m_Decoderservers, processname);
    } else {
      Log.e("DecodeManager", "Can't write symbology configuration to decode");
    }
  }

  private void doSaveSymbologyConfig() {
    LoadAllSymbologyId();
    this.mPrefs = this.mPrefsCopy;
    SharedPreferences Prefs = this.mOtherContex.getSharedPreferences(this.mPrefs, 3);

    SharedPreferences.Editor editor = Prefs.edit();

    for (int j = 0; j < this.mSymbologySettingMap.size(); j++) {
      Integer symbologyId = Integer.valueOf(((SymbologySetting)this.mSymbologySettingMap.get(j)).getSymbologyId());

      int size = ((SymbologySetting)this.mSymbologySettingMap.get(j)).GetConfigDataArry().size();

      int prolist = 0;
      int proValue = 0;
      boolean bStatus = false;
      for (int m = 0; m < size; m++)
      {
        SymbologyCommonProperty value = (SymbologyCommonProperty)((SymbologySetting)this.mSymbologySettingMap.get(j)).GetConfigDataArry().get(m);

        bStatus = ((SymbologySetting)this.mSymbologySettingMap.get(j)).getbSymbologyActive();

        int PropertyInt = value.PropertyId;
        prolist |= PropertyInt;

        if (value.checked) {
          proValue |= PropertyInt;
        }
        if (!bStatus) {
          proValue = 0;
        }
      }
      String strBarCodeName = this.mParseSymbologyProperty.findBarcodeNameFromId(symbologyId.intValue());

      editor.putInt(strBarCodeName + "PropertyEnableList", proValue);
      editor.putInt(strBarCodeName + "PropertyList", prolist);

      editor.putBoolean(strBarCodeName + " available ", bStatus);

      if (HasMinMaxProperty(symbologyId.intValue()))
      {
        MinMaxProperty min = getOneSymbologyMinProperty(symbologyId.intValue());
        MinMaxProperty max = getOneSymbologyMaxProperty(symbologyId.intValue());

        editor.putInt(strBarCodeName + "_Min_From", min.getValueFrom());

        editor.putInt(strBarCodeName + "_Min_To", min.getValueTo());

        editor.putInt(strBarCodeName + "_Min_Length", min.getValue());

        editor.putInt(strBarCodeName + "_Max_From", max.getValueFrom());

        editor.putInt(strBarCodeName + "_Max_To", max.getValueTo());

        editor.putInt(strBarCodeName + "_Max_Length", max.getValue());
      }

    }

    editor.putInt("OCRMode", this.mOcrMode);
    editor.putInt("OCRLength", this.mOcrLength);
    editor.putBoolean("OCRBSupport", this.mbOcrSupport);

    editor.commit();
  }

  private void SaveSymbologyProperty(int symbologyId, ArrayList<SymbologyPropertyStatus> OneCodeBarPropertyList, boolean bStatus)
  {
    if (OneCodeBarPropertyList == null) {
      return;
    }

    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return;
    }

    SymbologySetting SymSetting = new SymbologySetting();
    for (int i = 0; i < OneCodeBarPropertyList.size(); i++) {
      int id = ((SymbologyPropertyStatus)OneCodeBarPropertyList.get(i)).PropertyId;
      boolean checked = ((SymbologyPropertyStatus)OneCodeBarPropertyList.get(i)).checked;
      SymbologyCommonProperty value = new SymbologyCommonProperty();
      value.PropertyId = id;
      value.checked = checked;
      if (!bStatus) {
        value.checked = false;
      }

      SymSetting.GetConfigDataArry().add(value);
    }

    SymSetting.setSymbologyId(symbologyId);
    SymSetting.setbSymbologyActive(bStatus);
    this.mSymbologySettingMap.add(SymSetting);
  }

  private void SaveMinMaxProperty(int symbologyId, MinMaxProperty minmaxPro, MinMaxProperty maxminPro)
  {
    if ((symbologyId > 52) && (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++)
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        ((SymbologySetting)this.mSymbologySettingMap.get(i)).GetMinMaxPropertyArry().add(minmaxPro);

        ((SymbologySetting)this.mSymbologySettingMap.get(i)).GetMinMaxPropertyArry().add(maxminPro);
      }
  }

  private void LoadSymbologyConfigFromSharePreference(SharedPreferences Prefs, int symbologyId, int OneCodeBarDeafultProList, int OneCodeBarDefaultProValue, SymbologyActivityProperty Active, HaveMinMaxProperty bHaveMinMax, int minfrom, int minto, int minlength, int maxfrom, int maxto, int maxlength)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return;
    }

    String strBarcode = this.mParseSymbologyProperty.findBarcodeNameFromId(symbologyId);

    int PropertyValue = Prefs.getInt(strBarcode + "PropertyEnableList", OneCodeBarDefaultProValue);

    int Propertylist = Prefs.getInt(strBarcode + "PropertyList", OneCodeBarDeafultProList);

    boolean symbologyStatus = false;
    if (Active == SymbologyActivityProperty.active)
      symbologyStatus = Prefs.getBoolean(strBarcode + " available ", true);
    else {
      symbologyStatus = Prefs.getBoolean(strBarcode + " available ", false);
    }

    ArrayList codebarSymbologyList = this.mParseSymbologyProperty.ParseSymbologyPropertyint(Propertylist, PropertyValue);

    SaveSymbologyProperty(symbologyId, codebarSymbologyList, symbologyStatus);

    if (bHaveMinMax == HaveMinMaxProperty.HAVE_MINMAX_PROPERTY) {
      int Min_From = Prefs.getInt(strBarcode + "_Min_From", minfrom);
      int Min_To = Prefs.getInt(strBarcode + "_Min_To", minto);
      int MinLength = Prefs.getInt(strBarcode + "_Min_Length", minlength);

      int Max_From = Prefs.getInt(strBarcode + "_Max_From", maxfrom);
      int Max_To = Prefs.getInt(strBarcode + "_Max_To", maxto);
      int MaxLength = Prefs.getInt(strBarcode + "_Max_Length", maxlength);

      MinMaxProperty minmaxPro = new MinMaxProperty();
      minmaxPro.setMinMax(MinMaxProperty.MinMax.MIN);
      minmaxPro.setValue(MinLength);
      minmaxPro.setValueFrom(Min_From);
      minmaxPro.setValueTo(Min_To);

      MinMaxProperty maxminPro = new MinMaxProperty();
      maxminPro.setMinMax(MinMaxProperty.MinMax.MAX);
      maxminPro.setValue(MaxLength);
      maxminPro.setValueFrom(Max_From);
      maxminPro.setValueTo(Max_To);
      SaveMinMaxProperty(symbologyId, minmaxPro, maxminPro);
    }
  }

  public ArrayList<Integer> getAllCommonSymbologyId()
  {
    return (ArrayList)this.m_ArrayActiveCommonSymId.clone();
  }

  public ArrayList<Integer> getAllPostalSymbologyId()
  {
    return (ArrayList)this.m_ArrayActivePostalSymId.clone();
  }

  public ArrayList<Integer> getAllSymbologyId()
  {
    return (ArrayList)this.m_ArrayAllActiveSymId.clone();
  }

  private SymbologySetting getOneSymbologySetting(int symbologyId)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return null;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        return (SymbologySetting)this.mSymbologySettingMap.get(i);
      }
    }
    return null;
  }

  private void LoadAllSymbologyId()
  {
    this.m_ArrayAllActiveSymId.clear();
    this.m_ArrayActivePostalSymId.clear();
    this.m_ArrayActiveCommonSymId.clear();

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getbSymbologyActive()) {
        this.m_ArrayAllActiveSymId.add(Integer.valueOf(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId()));
      }

    }

    if (getbOcrSupport()) {
      this.m_ArrayAllActiveSymId.add(Integer.valueOf(14));
    }

    for (int i = 0; i < this.m_ArrayAllActiveSymId.size(); i++)
      if (-1 != this.mPostSymbology.findPostSymbologyId(((Integer)this.m_ArrayAllActiveSymId.get(i)).intValue()))
      {
        this.m_ArrayActivePostalSymId.add(this.m_ArrayAllActiveSymId.get(i));
      } else if (((Integer)this.m_ArrayAllActiveSymId.get(i)).intValue() != 14)
        this.m_ArrayActiveCommonSymId.add(this.m_ArrayAllActiveSymId.get(i));
  }

  public boolean getOneSymbologyStatus(int symbologyId)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return false;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);

        for (int j = 0; j < symopData.GetConfigDataArry().size(); j++) {
          SymbologyCommonProperty pro = (SymbologyCommonProperty)symopData.GetConfigDataArry().get(j);

          if (pro.PropertyId == 1) {
            return pro.checked;
          }
        }
      }
    }
    return false;
  }

  public ArrayList<SymbologyCommonProperty> getOneSymbologyProerty(int symbologyId)
  {
    if (this.m_arrySymProperty != null) {
      this.m_arrySymProperty.clear();
    }

    SymbologySetting symsetting = getOneSymbologySetting(symbologyId);

    int num = symsetting.GetConfigDataArry().size();

    for (int i = 0; i < num; i++) {
      SymbologyCommonProperty symcompro = new SymbologyCommonProperty();
      symcompro.checked = ((SymbologyCommonProperty)symsetting.GetConfigDataArry().get(i)).checked;
      symcompro.PropertyId = ((SymbologyCommonProperty)symsetting.GetConfigDataArry().get(i)).PropertyId;
      this.m_arrySymProperty.add(i, symcompro);
    }

    return this.m_arrySymProperty;
  }

  public boolean modifyOneSymbologyProperty(int symbologyId, SymbologyCommonProperty SymComPro)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return false;
    }
    SymbologySetting symsetting = getOneSymbologySetting(symbologyId);

    int num = symsetting.GetConfigDataArry().size();

    for (int i = 0; i < num; i++) {
      if (SymComPro.PropertyId == ((SymbologyCommonProperty)symsetting.GetConfigDataArry().get(i)).PropertyId) {
        ((SymbologyCommonProperty)symsetting.GetConfigDataArry().get(i)).PropertyId = SymComPro.PropertyId;
        ((SymbologyCommonProperty)symsetting.GetConfigDataArry().get(i)).checked = SymComPro.checked;
        return true;
      }
    }

    return false;
  }

  public boolean modifyOneSymbologyStatus(int symbologyId, boolean bStatus)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return false;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);
        for (int j = 0; j < symopData.GetConfigDataArry().size(); j++) {
          SymbologyCommonProperty symPro = (SymbologyCommonProperty)symopData.GetConfigDataArry().get(j);

          if (symPro.PropertyId == 1) {
            symPro.checked = bStatus;

            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean HasMinMaxProperty(int symbologyId) {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return false;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);
        if (symopData.GetMinMaxPropertyArry().size() != 0) {
          return true;
        }
      }
    }
    return false;
  }

  public MinMaxProperty getOneSymbologyMinValue(int symbologyId)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return null;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);

        for (int j = 0; j < symopData.GetMinMaxPropertyArry().size(); j++) {
          MinMaxProperty value = (MinMaxProperty)symopData.GetMinMaxPropertyArry().get(j);

          if (value.getMinMax() == MinMaxProperty.MinMax.MIN) {
            return value;
          }
        }
      }
    }

    return null;
  }

  public MinMaxProperty getOneSymbologyMaxValue(int symbologyId)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return null;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);

        for (int j = 0; j < symopData.GetMinMaxPropertyArry().size(); j++) {
          MinMaxProperty value = (MinMaxProperty)symopData.GetMinMaxPropertyArry().get(j);

          if (value.getMinMax() == MinMaxProperty.MinMax.MAX) {
            return value;
          }
        }
      }
    }
    return null;
  }

  public boolean modifyOneSymbologyMinValue(int symbologyId, int value)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return false;
    }

    if (!HasMinMaxProperty(symbologyId)) {
      return false;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);

        for (int j = 0; j < symopData.GetMinMaxPropertyArry().size(); j++) {
          MinMaxProperty minValue = (MinMaxProperty)symopData.GetMinMaxPropertyArry().get(j);

          if (minValue.getMinMax() == MinMaxProperty.MinMax.MIN)
          {
            minValue.setValue(value);

            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean modifyOneSymbologyMaxValue(int symbologyId, int value)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return false;
    }
    if (!HasMinMaxProperty(symbologyId)) {
      return false;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);

        for (int j = 0; j < symopData.GetMinMaxPropertyArry().size(); j++) {
          MinMaxProperty maxValue = (MinMaxProperty)symopData.GetMinMaxPropertyArry().get(j);

          if (maxValue.getMinMax() == MinMaxProperty.MinMax.MAX)
          {
            maxValue.setValue(value);

            return true;
          }
        }
      }
    }
    return false;
  }

  private MinMaxProperty getOneSymbologyMinProperty(int symbologyId)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return null;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId)
      {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);

        for (int j = 0; j < symopData.GetMinMaxPropertyArry().size(); j++) {
          MinMaxProperty value = (MinMaxProperty)symopData.GetMinMaxPropertyArry().get(j);

          if (value.getMinMax() == MinMaxProperty.MinMax.MIN) {
            return value;
          }
        }
      }
    }
    return null;
  }

  private MinMaxProperty getOneSymbologyMaxProperty(int symbologyId)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return null;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId)
      {
        SymbologySetting symopData = (SymbologySetting)this.mSymbologySettingMap.get(i);

        for (int j = 0; j < symopData.GetMinMaxPropertyArry().size(); j++) {
          MinMaxProperty value = (MinMaxProperty)symopData.GetMinMaxPropertyArry().get(j);

          if (value.getMinMax() == MinMaxProperty.MinMax.MAX) {
            return value;
          }
        }
      }
    }
    return null;
  }

  public boolean removeOneSymFromConfig(int symbologyId)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return false;
    }

    if (symbologyId == 14) {
      this.mbOcrSupport = false;

      return true;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        ((SymbologySetting)this.mSymbologySettingMap.get(i)).setbSymbologyActive(false);

        return true;
      }
    }

    Log.e("DecodeManager", "Can't remove the symbology from configuration!");
    return false;
  }

  public boolean removeAllSymFromConfig()
  {
    this.mbOcrSupport = false;

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      ((SymbologySetting)this.mSymbologySettingMap.get(i)).setbSymbologyActive(false);
    }
    return true;
  }

  public boolean restoreDefaultSymToConfig()
  {
    this.mbOcrSupport = true;

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      ((SymbologySetting)this.mSymbologySettingMap.get(i)).setbSymbologyActive(true);
    }
    return true;
  }

  public boolean addOneSymToConfig(int symbologyId)
  {
    if ((symbologyId > 52) || (symbologyId < 0))
    {
      Log.e("DecodeManager", "SymbologyID is not in range!");
      return false;
    }
    if (symbologyId == 14) {
      this.mbOcrSupport = true;
      return true;
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      if (((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId() == symbologyId) {
        ((SymbologySetting)this.mSymbologySettingMap.get(i)).setbSymbologyActive(true);
        return true;
      }
    }

    Log.e("DecodeManager", "Can't add the symbology to configuration!");
    return false;
  }

  private void writeSymbologyConfigureWithProcess(IDecoderService Decoderservers, String processname)
  {
    SymbologyConfig symconfig = new SymbologyConfig();
    int flag = 0;
    int mask = 1;
    int minlength = 0;
    int maxlength = 0;
    this.m_Decoderservers = Decoderservers;

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      Integer symID = Integer.valueOf(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId());
      try {
        this.m_Decoderservers.disableSymbology(symID.intValue());
      }
      catch (RemoteException e) {
        e.printStackTrace();
      }
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++)
    {
      int size = ((SymbologySetting)this.mSymbologySettingMap.get(i)).GetConfigDataArry().size();
      int prolist = 0;
      int proValue = 0;
      boolean bStatus = false;
      mask = 1;
      minlength = 0;
      maxlength = 0;

      for (int j = 0; j < size; j++)
      {
        SymbologyCommonProperty symPro = (SymbologyCommonProperty)((SymbologySetting)this.mSymbologySettingMap.get(i)).GetConfigDataArry().get(j);

        bStatus = ((SymbologySetting)this.mSymbologySettingMap.get(i)).getbSymbologyActive();

        int PropertyInt = symPro.PropertyId;
        prolist |= PropertyInt;

        if (symPro.checked) {
          proValue |= PropertyInt;
        }

        if (!bStatus) {
          proValue = 0;
        }
      }

      flag = proValue;

      if (HasMinMaxProperty(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId()))
      {
        MinMaxProperty min = getOneSymbologyMinProperty(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId());

        MinMaxProperty max = getOneSymbologyMaxProperty(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId());

        mask = 7;
        minlength = min.getValue();
        maxlength = max.getValue();
      }

      symconfig.Flags = flag;
      symconfig.Mask = mask;
      symconfig.MaxLength = maxlength;
      symconfig.MinLength = minlength;
      symconfig.symID = ((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId();
      try
      {
        this.m_Decoderservers.setSymbologyConfigWithProcess(processname, symconfig);
      }
      catch (RemoteException e)
      {
        e.printStackTrace();
      }
    }
    byte ocrType = 3;

    int ocrlength = getOcrLength();
    byte[] Template = new byte[ocrlength + 3];

    Template[0] = 1;
    Template[1] = ocrType;
    Template[(ocrlength + 2)] = 0;
    for (int j = 0; j < ocrlength; j++) {
      Template[(j + 2)] = 7;
    }
    try
    {
      this.m_Decoderservers.setOCRTemplates(5);

      this.m_Decoderservers.setOCRUserTemplate(getOcrMode(), Template);
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void writeSymbologyConfigure(IDecoderService Decoderservers)
  {
    SymbologyConfig symconfig = new SymbologyConfig();
    int flag = 0;
    int mask = 1;
    int minlength = 0;
    int maxlength = 0;
    this.m_Decoderservers = Decoderservers;

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++) {
      Integer symID = Integer.valueOf(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId());
      try {
        this.m_Decoderservers.disableSymbology(symID.intValue());
      }
      catch (RemoteException e) {
        e.printStackTrace();
      }
    }

    for (int i = 0; i < this.mSymbologySettingMap.size(); i++)
    {
      int size = ((SymbologySetting)this.mSymbologySettingMap.get(i)).GetConfigDataArry().size();
      int prolist = 0;
      int proValue = 0;
      boolean bStatus = false;
      mask = 1;
      minlength = 0;
      maxlength = 0;

      for (int j = 0; j < size; j++)
      {
        SymbologyCommonProperty symPro = (SymbologyCommonProperty)((SymbologySetting)this.mSymbologySettingMap.get(i)).GetConfigDataArry().get(j);

        bStatus = ((SymbologySetting)this.mSymbologySettingMap.get(i)).getbSymbologyActive();

        int PropertyInt = symPro.PropertyId;
        prolist |= PropertyInt;

        if (symPro.checked) {
          proValue |= PropertyInt;
        }

        if (!bStatus) {
          proValue = 0;
        }
      }

      flag = proValue;

      if (HasMinMaxProperty(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId()))
      {
        MinMaxProperty min = getOneSymbologyMinProperty(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId());

        MinMaxProperty max = getOneSymbologyMaxProperty(((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId());

        mask = 7;
        minlength = min.getValue();
        maxlength = max.getValue();
      }

      symconfig.Flags = flag;
      symconfig.Mask = mask;
      symconfig.MaxLength = maxlength;
      symconfig.MinLength = minlength;
      symconfig.symID = ((SymbologySetting)this.mSymbologySettingMap.get(i)).getSymbologyId();
      try
      {
        this.m_Decoderservers.setSymbologyConfig(symconfig);
      }
      catch (RemoteException e)
      {
        e.printStackTrace();
      }
    }
    byte ocrType = 3;

    int ocrlength = getOcrLength();
    byte[] Template = new byte[ocrlength + 3];

    Template[0] = 1;
    Template[1] = ocrType;
    Template[(ocrlength + 2)] = 0;
    for (int j = 0; j < ocrlength; j++) {
      Template[(j + 2)] = 7;
    }
    try
    {
      this.m_Decoderservers.setOCRTemplates(5);

      this.m_Decoderservers.setOCRUserTemplate(getOcrMode(), Template);
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private static enum SymbologyActivityProperty
  {
    inactive, active;
  }

  private static enum HaveMinMaxProperty
  {
    NO_MINMAX_PROPERTY, HAVE_MINMAX_PROPERTY;
  }
}
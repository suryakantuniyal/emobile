package com.android.emobilepos.initialization;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.android.emobilepos.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EMobilePOSGeneralTest {

    @Rule
    public ActivityTestRule<SelectAccount_FA> mActivityTestRule = new ActivityTestRule<>(SelectAccount_FA.class);

    private static void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Initialization() {
        ViewInteraction myEditText = onView(
                withId(R.id.initAccountNumber));
        myEditText.perform(scrollTo(), click());

        ViewInteraction myEditText2 = onView(
                withId(R.id.initAccountNumber));
        myEditText2.perform(scrollTo(), replaceText("150023120409"), closeSoftKeyboard());

        ViewInteraction myEditText3 = onView(
                withId(R.id.initPassword));
        myEditText3.perform(scrollTo(), replaceText("enabler"), closeSoftKeyboard());

        ViewInteraction button = onView(
                allOf(withId(R.id.loginButton), withText("Log In")));
        button.perform(scrollTo(), click());
        ViewInteraction textView = onView(
                allOf(withId(R.id.employeeName), withText("Android Employee 2"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.employeeListView),
                                        1),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Android Employee 2")));

        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(
                        withId(R.id.employeeListView),
                        1),
                        isDisplayed()));
        linearLayout.perform(click());
        ViewInteraction button2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"), isDisplayed()));
        button2.perform(click());
        ViewInteraction myEditText4 = onView(
                withId(R.id.regPassword1));
        myEditText4.perform(scrollTo(), click());

        ViewInteraction myEditText5 = onView(
                withId(R.id.regPassword1));
        myEditText5.perform(scrollTo(), click());

        ViewInteraction myEditText6 = onView(
                withId(R.id.regPassword1));
        myEditText6.perform(scrollTo(), replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction myEditText7 = onView(
                withId(R.id.regPassword2));
        myEditText7.perform(scrollTo(), replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.setPasswordButton), withText("Set Password")));
        button3.perform(scrollTo(), click());

        ViewInteraction myEditText8 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText8.perform(click());

        ViewInteraction myEditText9 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText9.perform(replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button4.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withText("Settings"), isDisplayed()));
        textView2.perform(click());

        ViewInteraction button5 = onView(
                allOf(withId(R.id.btnAdminSetting), withText("Admin Settings")));
        button5.perform(scrollTo(), click());

        ViewInteraction myEditText10 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText10.perform(click());

        ViewInteraction myEditText11 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText11.perform(replaceText("admin"), closeSoftKeyboard());

        ViewInteraction button6 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button6.perform(click());

        ViewInteraction linearLayout2 = onView(
                allOf(childAtPosition(
                        withId(android.R.id.list),
                        8),
                        isDisplayed()));
        linearLayout2.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.setting_list), isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction linearLayout3 = onView(
                allOf(childAtPosition(
                        withId(android.R.id.list),
                        1),
                        isDisplayed()));
        linearLayout3.perform(click());

        pressBack();
    }

    @Test
    public void SalesReceiptCash() {
        ViewInteraction myEditText9 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText9.perform(replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button4.perform(click());
        pause();
        ViewInteraction textView3 = onView(
                allOf(withText("Sales"), isDisplayed()));
        textView3.perform(click());
        pause();

        ViewInteraction linearLayout15 = onView(
                allOf(withId(R.id.relativeLayout1),
                        childAtPosition(
                                withId(R.id.salesGridLayout),
                                1),
                        isDisplayed()));
        linearLayout15.perform(click());
        pause();
        ViewInteraction imageView21 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        4))),
                        isDisplayed()));
        imageView21.perform(click());
        pause();
        ViewInteraction imageView22 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView22.perform(click());
        pause();
        ViewInteraction imageView23 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView23.perform(click());
        pause();
        ViewInteraction button37 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button37.perform(click());
        pause();
        ViewInteraction button38 = onView(
                allOf(withId(R.id.btnCheckOut), withText("Checkout"),
                        withParent(withId(R.id.headerTitleContainer)),
                        isDisplayed()));
        button38.perform(click());
        pause();
        ViewInteraction button39 = onView(
                allOf(withId(R.id.btnDlogRight), withText("No"), isDisplayed()));
        button39.perform(click());
        pause();
        ViewInteraction button40 = onView(
                allOf(withId(R.id.OKButton), withText("OK")));
        button40.perform(scrollTo(), click());

        ViewInteraction linearLayout16 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.cardsListview),
                                withParent(withId(R.id.parentLayout))),
                        2),
                        isDisplayed()));
        linearLayout16.perform(click());
        pause();
        ViewInteraction button41 = onView(
                allOf(withId(R.id.exactAmountBut), withText("Exact"), isDisplayed()));
        button41.perform(click());
        pause();
        ViewInteraction button42 = onView(
                allOf(withId(R.id.processCashBut), withText("Process"), isDisplayed()));
        button42.perform(click());
        pause();
        ViewInteraction button43 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button43.perform(click());

    }

    @Test
    public void SalesReceiptHold() {
        ViewInteraction myEditText9 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText9.perform(replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button4.perform(click());
        pause();
        ViewInteraction textView3 = onView(
                allOf(withText("Sales"), isDisplayed()));
        textView3.perform(click());
        pause();

        ViewInteraction linearLayout9 = onView(
                allOf(withId(R.id.relativeLayout1),
                        childAtPosition(
                                withId(R.id.salesGridLayout),
                                0),
                        isDisplayed()));
        linearLayout9.perform(click());
        pause();
        ViewInteraction imageView14 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        4))),
                        isDisplayed()));
        imageView14.perform(click());
        pause();
        ViewInteraction imageView15 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView15.perform(click());
        pause();
        ViewInteraction imageView16 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView16.perform(click());
        pause();
        ViewInteraction button22 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button22.perform(click());
        pause();
        ViewInteraction imageView17 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        1))),
                        isDisplayed()));
        imageView17.perform(click());
        pause();
        ViewInteraction button23 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button23.perform(click());
        pause();
        ViewInteraction button24 = onView(
                allOf(withId(R.id.holdButton), withText("Hold"), isDisplayed()));
        button24.perform(click());
        pause();
        ViewInteraction myEditText16 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText16.perform(replaceText("hold"), closeSoftKeyboard());

        ViewInteraction button25 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button25.perform(click());
        pause();
        ViewInteraction linearLayout10 = onView(
                allOf(withId(R.id.relativeLayout1),
                        childAtPosition(
                                withId(R.id.salesGridLayout),
                                7),
                        isDisplayed()));
        linearLayout10.perform(click());
        pause();
        ViewInteraction linearLayout11 = onView(
                allOf(childAtPosition(
                        withId(R.id.onHoldListView),
                        0),
                        isDisplayed()));
        linearLayout11.perform(click());
        pause();
        pause();
        ViewInteraction button26 = onView(
                allOf(withId(R.id.btnDlogLeft), withText("Open"), isDisplayed()));
        button26.perform(click());
        pause();
        ViewInteraction imageView18 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        4))),
                        isDisplayed()));
        imageView18.perform(click());
        pause();
        ViewInteraction imageView19 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        2))),
                        isDisplayed()));
        imageView19.perform(click());
        pause();
        ViewInteraction imageView20 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView20.perform(click());
        pause();
        ViewInteraction button27 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button27.perform(click());
        pause();
        ViewInteraction button28 = onView(
                allOf(withId(R.id.holdButton), withText("Hold"), isDisplayed()));
        button28.perform(click());
        pause();
        ViewInteraction linearLayout12 = onView(
                allOf(withId(R.id.relativeLayout1),
                        childAtPosition(
                                withId(R.id.salesGridLayout),
                                7),
                        isDisplayed()));
        linearLayout12.perform(click());
        pause();
        ViewInteraction linearLayout13 = onView(
                allOf(childAtPosition(
                        withId(R.id.onHoldListView),
                        0),
                        isDisplayed()));
        linearLayout13.perform(click());
        pause();
        ViewInteraction button29 = onView(
                allOf(withId(R.id.btnDlogLeft), withText("Open"), isDisplayed()));
        button29.perform(click());
        pause();
        ViewInteraction button30 = onView(
                allOf(withId(R.id.btnCheckOut), withText("Checkout"),
                        withParent(withId(R.id.headerTitleContainer)),
                        isDisplayed()));
        button30.perform(click());
        pause();
        ViewInteraction button31 = onView(
                allOf(withId(R.id.btnDlogRight), withText("No"), isDisplayed()));
        button31.perform(click());
        pause();
        ViewInteraction button32 = onView(
                allOf(withId(R.id.OKButton), withText("OK")));
        button32.perform(scrollTo(), click());

        ViewInteraction linearLayout14 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.cardsListview),
                                withParent(withId(R.id.parentLayout))),
                        2),
                        isDisplayed()));
        linearLayout14.perform(click());
        pause();
        ViewInteraction button33 = onView(
                allOf(withId(R.id.btnFive), withText("$5"), isDisplayed()));
        button33.perform(click());
        pause();
        ViewInteraction button34 = onView(
                allOf(withId(R.id.exactAmountBut), withText("Exact"), isDisplayed()));
        button34.perform(click());
        pause();
        ViewInteraction button35 = onView(
                allOf(withId(R.id.processCashBut), withText("Process"), isDisplayed()));
        button35.perform(click());
        pause();
        ViewInteraction button36 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button36.perform(click());

    }

    @Test
    public void SalesReceiptDiscountCredit() {
        ViewInteraction myEditText9 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText9.perform(replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button4.perform(click());
        pause();
        ViewInteraction textView3 = onView(
                allOf(withText("Sales"), isDisplayed()));
        textView3.perform(click());
        pause();

        ViewInteraction linearLayout5 = onView(
                allOf(withId(R.id.relativeLayout1),
                        childAtPosition(
                                withId(R.id.salesGridLayout),
                                0),
                        isDisplayed()));
        linearLayout5.perform(click());
        pause();
        ViewInteraction imageView = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        4))),
                        isDisplayed()));
        imageView.perform(click());
        pause();
        ViewInteraction imageView2 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView2.perform(click());
        pause();
        ViewInteraction imageView3 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        1))),
                        isDisplayed()));
        imageView3.perform(click());
        pause();
        ViewInteraction button10 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button10.perform(click());
        pause();
        ViewInteraction imageView4 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView4.perform(click());
        pause();
        ViewInteraction button11 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button11.perform(click());
        pause();
        ViewInteraction button12 = onView(
                allOf(withText("FOODS"),
                        withParent(withId(R.id.categoriesButtonLayoutHolder)),
                        isDisplayed()));
        button12.perform(click());
        pause();
        ViewInteraction imageView5 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        4))),
                        isDisplayed()));
        imageView5.perform(click());
        pause();
        ViewInteraction imageView6 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        1))),
                        isDisplayed()));
        imageView6.perform(click());
        pause();
        ViewInteraction imageView7 = onView(
                allOf(withText("Pepperoni"), isDisplayed()));
        imageView7.perform(click());
        pause();
        ViewInteraction imageView8 = onView(
                allOf(withText("Chorizo"), isDisplayed()));
        imageView8.perform(click());
        pause();
        ViewInteraction imageView9 = onView(
                allOf(withText("Pepperoni"), isDisplayed()));
        imageView9.perform(click());
        pause();

//        ViewInteraction imageView11 = onView(
//                allOf(withId(R.id.gridViewImage),
//                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
//                                withParent(withId(R.id.addonParentHolder))))));
//        imageView11.perform(scrollTo(), click());
//
//        ViewInteraction imageView12 = onView(
//                allOf(withId(R.id.data_item_image), isDisplayed()));
//        imageView12.perform(click());
//        pause();
//        ViewInteraction imageView13 = onView(
//                allOf(withId(R.id.data_item_image), isDisplayed()));
//        imageView13.perform(click());
//        pause();
        ViewInteraction button13 = onView(
                allOf(withId(R.id.addonDoneButton), withText("Done"), isDisplayed()));
        button13.perform(click());
        pause();
        ViewInteraction linearLayout6 = onView(
                allOf(childAtPosition(
                        withId(R.id.pickerLV),
                        7),
                        isDisplayed()));
        linearLayout6.perform(click());
        pause();
        ViewInteraction linearLayout7 = onView(
                allOf(childAtPosition(
                        withId(R.id.dlgListView),
                        1),
                        isDisplayed()));
        linearLayout7.perform(click());
        pause();
        ViewInteraction button14 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button14.perform(click());
        pause();
        ViewInteraction button15 = onView(
                allOf(withId(R.id.btnCheckOut), withText("Checkout"),
                        withParent(withId(R.id.headerTitleContainer)),
                        isDisplayed()));
        button15.perform(click());
        pause();
        ViewInteraction button16 = onView(
                allOf(withId(R.id.btnDlogRight), withText("No"), isDisplayed()));
        button16.perform(click());
        pause();
        ViewInteraction button17 = onView(
                allOf(withId(R.id.OKButton), withText("OK")));
        button17.perform(scrollTo(), click());

        ViewInteraction linearLayout8 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.cardsListview),
                                withParent(withId(R.id.parentLayout))),
                        0),
                        isDisplayed()));
        linearLayout8.perform(click());
        pause();
        ViewInteraction myEditText12 = onView(
                withId(R.id.cardNumEdit));
        myEditText12.perform(scrollTo(), replaceText("4111111111111111"), closeSoftKeyboard());

        ViewInteraction myEditText13 = onView(
                withId(R.id.processCardSeccode));
        myEditText13.perform(scrollTo(), replaceText("123"), closeSoftKeyboard());

        ViewInteraction myEditText14 = onView(
                withId(R.id.monthEdit));
        myEditText14.perform(scrollTo(), replaceText("12"), closeSoftKeyboard());

        ViewInteraction myEditText15 = onView(
                withId(R.id.yearEdit));
        myEditText15.perform(scrollTo(), replaceText("2017"), closeSoftKeyboard());

        ViewInteraction button18 = onView(
                allOf(withId(R.id.exactAmountBut), withText("Exact"), isDisplayed()));
        button18.perform(click());
        pause();
        ViewInteraction button19 = onView(
                allOf(withId(R.id.processButton), withText("Process"), isDisplayed()));
        button19.perform(click());
        pause();
        ViewInteraction button20 = onView(
                allOf(withId(R.id.acceptBut), withText("Accept"), isDisplayed()));
        button20.perform(click());
        pause();
        ViewInteraction button21 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button21.perform(click());

    }

    @Test
    public void SalesReceiptDiscountCash() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction myEditText9 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText9.perform(replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button4.perform(click());
        pause();
        ViewInteraction textView3 = onView(
                allOf(withText("Sales"), isDisplayed()));
        textView3.perform(click());
        pause();
        ViewInteraction linearLayout4 = onView(
                allOf(withId(R.id.relativeLayout1),
                        childAtPosition(
                                withId(R.id.salesGridLayout),
                                0),
                        isDisplayed()));
        linearLayout4.perform(click());
        pause();
        ViewInteraction imageView = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        4))),
                        isDisplayed()));
        imageView.perform(click());
        pause();
        ViewInteraction imageView2 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView2.perform(click());
        pause();
        ViewInteraction imageView3 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        0))),
                        isDisplayed()));
        imageView3.perform(click());
        pause();
        ViewInteraction button7 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button7.perform(click());
        pause();
        ViewInteraction imageView4 = onView(
                allOf(withId(R.id.gridViewImage),
                        withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                childAtPosition(
                                        withId(R.id.catalogListview),
                                        1))),
                        isDisplayed()));
        imageView4.perform(click());
        pause();
        ViewInteraction linearLayout5 = onView(
                allOf(childAtPosition(
                        withId(R.id.pickerLV),
                        7),
                        isDisplayed()));
        linearLayout5.perform(click());
        pause();
        ViewInteraction linearLayout6 = onView(
                allOf(childAtPosition(
                        withId(R.id.dlgListView),
                        2),
                        isDisplayed()));
        linearLayout6.perform(click());
        pause();
        ViewInteraction button8 = onView(
                allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                        withParent(withId(R.id.row1)),
                        isDisplayed()));
        button8.perform(click());
        pause();
        ViewInteraction spinner = onView(
                allOf(withId(R.id.globalDiscountSpinner), isDisplayed()));
        spinner.perform(click());
        pause();
        ViewInteraction tt = onView(
                allOf(withText("10%")));

        tt.perform(click());

//
//        ViewInteraction linearLayout7 = onView(
//                allOf(childAtPosition(
//                        withId(R.id.globalDiscountSpinner),
//                        2),
//                        isDisplayed()));
//
////        ViewInteraction linearLayout7 = onView(
////                allOf(withClassName(is("android.widget.LinearLayout")), isDisplayed()));
//        linearLayout7.perform(click());
        pause();
        ViewInteraction button9 = onView(
                allOf(withId(R.id.btnCheckOut), withText("Checkout"),
                        withParent(withId(R.id.headerTitleContainer)),
                        isDisplayed()));
        button9.perform(click());
        pause();
        ViewInteraction button10 = onView(
                allOf(withId(R.id.btnDlogRight), withText("No"), isDisplayed()));
        button10.perform(click());
        pause();
        ViewInteraction button11 = onView(
                allOf(withId(R.id.OKButton), withText("OK")));
        button11.perform(scrollTo(), click());

        ViewInteraction linearLayout8 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.cardsListview),
                                withParent(withId(R.id.parentLayout))),
                        2),
                        isDisplayed()));
        linearLayout8.perform(click());
        pause();
        ViewInteraction button12 = onView(
                allOf(withId(R.id.exactAmountBut), withText("Exact"), isDisplayed()));
        button12.perform(click());
        pause();
        ViewInteraction button13 = onView(
                allOf(withId(R.id.processCashBut), withText("Process"), isDisplayed()));
        button13.perform(click());
        pause();
        ViewInteraction button14 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button14.perform(click());
        pause();
    }


    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}

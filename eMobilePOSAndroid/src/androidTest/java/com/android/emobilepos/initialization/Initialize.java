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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Initialize {

    @Rule
    public ActivityTestRule<SelectAccount_FA> mActivityTestRule = new ActivityTestRule<>(SelectAccount_FA.class);

    @Test
    public void initialize() {

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

        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(
                        withId(R.id.employeeListView),
                        2),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"), isDisplayed()));
        button2.perform(click());

        ViewInteraction myEditText4 = onView(
                withId(R.id.regPassword1));
        myEditText4.perform(scrollTo(), replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction myEditText5 = onView(
                withId(R.id.regPassword2));
        myEditText5.perform(scrollTo(), replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.setPasswordButton), withText("Set Password")));
        button3.perform(scrollTo(), click());

        ViewInteraction myEditText6 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText6.perform(click());

        ViewInteraction myEditText7 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText7.perform(replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button4.perform(click());
        for (int i = 0; i < 5; i++) {

            ViewInteraction linearLayout2 = onView(
                    allOf(withId(R.id.relativeLayout1),
                            childAtPosition(
                                    withId(R.id.salesGridLayout),
                                    0),
                            isDisplayed()));
            linearLayout2.perform(click());

            ViewInteraction imageView = onView(
                    allOf(withId(R.id.gridViewImage),
                            withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                    childAtPosition(
                                            withId(R.id.catalogListview),
                                            2))),
                            isDisplayed()));
            imageView.perform(click());

            ViewInteraction button5 = onView(
                    allOf(withId(R.id.pickerHeaderButton), withText("Add"),
                            withParent(withId(R.id.row1)),
                            isDisplayed()));
            button5.perform(click());

            ViewInteraction button6 = onView(
                    allOf(withId(R.id.btnCheckOut), withText("Checkout"),
                            withParent(withId(R.id.headerTitleContainer)),
                            isDisplayed()));
            button6.perform(click());

            ViewInteraction button7 = onView(
                    allOf(withId(R.id.btnDlogRight), withText("No"), isDisplayed()));
            button7.perform(click());

            ViewInteraction button8 = onView(
                    allOf(withId(R.id.OKButton), withText("OK")));
            button8.perform(scrollTo(), click());

            ViewInteraction linearLayout3 = onView(
                    allOf(childAtPosition(
                            allOf(withId(R.id.cardsListview),
                                    withParent(withId(R.id.parentLayout))),
                            2),
                            isDisplayed()));
            linearLayout3.perform(click());

            ViewInteraction button9 = onView(
                    allOf(withId(R.id.btnTwenty), withText("$20"), isDisplayed()));
            button9.perform(click());

            ViewInteraction button10 = onView(
                    allOf(withId(R.id.processCashBut), withText("Process"), isDisplayed()));
            button10.perform(click());

            ViewInteraction button11 = onView(
                    allOf(withText("OK"), isDisplayed()));
            button11.perform(click());

            ViewInteraction button12 = onView(
                    allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
            button12.perform(click());

        }
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

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
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class General_eMobilePOS_Test {

    @Rule
    public ActivityTestRule<SelectAccount_FA> mActivityTestRule = new ActivityTestRule<>(SelectAccount_FA.class);

    @Test
    public void initialization() {
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

        ViewInteraction button3 = onView(
                allOf(withId(R.id.setPasswordButton),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
                                        0),
                                4),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        ViewInteraction myEditText4 = onView(
                withId(R.id.regPassword1));
        myEditText4.perform(scrollTo(), click());

        ViewInteraction myEditText5 = onView(
                withId(R.id.regPassword1));
        myEditText5.perform(scrollTo(), replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction myEditText6 = onView(
                withId(R.id.regPassword2));
        myEditText6.perform(scrollTo(), replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.setPasswordButton), withText("Set Password")));
        button4.perform(scrollTo(), click());

        ViewInteraction myEditText7 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText7.perform(click());
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

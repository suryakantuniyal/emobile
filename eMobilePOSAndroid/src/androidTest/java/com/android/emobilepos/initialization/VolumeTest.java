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
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VolumeTest {
    private boolean isInit = true;
    @Rule
    public ActivityTestRule<SelectAccount_FA> mActivityTestRule = new ActivityTestRule<>(SelectAccount_FA.class);

    @Test
    public void volumeTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        if (isInit) {

            ViewInteraction myEditText = onView(
                    withId(R.id.initAccountNumber));

            myEditText.perform(scrollTo(), click());

            ViewInteraction myEditText2 = onView(
                    withId(R.id.initAccountNumber));
            myEditText2.perform(scrollTo(), replaceText("150023120409"), closeSoftKeyboard());

            ViewInteraction myEditText3 = onView(
                    withId(R.id.initPassword));
            myEditText3.perform(scrollTo(), replaceText("enabler"), closeSoftKeyboard());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//        ViewInteraction myEditText4 = onView(
//                allOf(withId(R.id.initPassword), withText("ena")));
//        myEditText4.perform(scrollTo(), replaceText("enab"), closeSoftKeyboard());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        ViewInteraction myEditText5 = onView(
//                allOf(withId(R.id.initPassword), withText("enab")));
//        myEditText5.perform(scrollTo(), replaceText("enabl"), closeSoftKeyboard());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        ViewInteraction myEditText6 = onView(
//                allOf(withId(R.id.initPassword), withText("enabl")));
//        myEditText6.perform(scrollTo(), replaceText("enabler"), closeSoftKeyboard());

            ViewInteraction button = onView(
                    allOf(withId(R.id.loginButton), withText("Log In")));
            button.perform(scrollTo(), click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction linearLayout = onView(
                    allOf(childAtPosition(
                            withId(R.id.employeeListView),
                            0),
                            isDisplayed()));
            linearLayout.perform(click());

            ViewInteraction button2 = onView(
                    allOf(withId(android.R.id.button1), withText("OK"), isDisplayed()));
            button2.perform(click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction myEditText7 = onView(
                    withId(R.id.regPassword1));
            myEditText7.perform(scrollTo(), replaceText("wwwww"), closeSoftKeyboard());

            ViewInteraction myEditText8 = onView(
                    withId(R.id.regPassword2));
            myEditText8.perform(scrollTo(), replaceText("wwwww"), closeSoftKeyboard());

            ViewInteraction button3 = onView(
                    allOf(withId(R.id.setPasswordButton), withText("Set Password")));
            button3.perform(scrollTo(), click());
        }
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction myEditText9 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText9.perform(click());

        ViewInteraction myEditText10 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText10.perform(replaceText("wwwww"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button4.perform(click());

        ViewInteraction textView = onView(
                allOf(withText("Settings"), isDisplayed()));
        textView.perform(click());
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction button5 = onView(
                allOf(withId(R.id.btnAdminSetting), withText("Admin Settings")));
        button5.perform(scrollTo(), click());

        ViewInteraction myEditText11 = onView(
                allOf(withId(R.id.dlogFieldSingle), isDisplayed()));
        myEditText11.perform(replaceText("admin"), closeSoftKeyboard());

        ViewInteraction button6 = onView(
                allOf(withId(R.id.btnDlogSingle), withText("OK"), isDisplayed()));
        button6.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction linearLayout2 = onView(
                allOf(childAtPosition(
                        withId(android.R.id.list),
                        1),
                        isDisplayed()));
        linearLayout2.perform(click());

        ViewInteraction linearLayout3 = onView(
                allOf(childAtPosition(
                        withId(android.R.id.list),
                        4),
                        isDisplayed()));
        linearLayout3.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.setting_list), isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction linearLayout4 = onView(
                allOf(childAtPosition(
                        withId(android.R.id.list),
                        1),
                        isDisplayed()));
        linearLayout4.perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pressBack();

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 100; i++) {


            ViewInteraction textView2 = onView(
                    allOf(withText("Sales"), isDisplayed()));
            textView2.perform(click());

            ViewInteraction textView3 = onView(
                    allOf(withText("Sales"), isDisplayed()));
            textView3.perform(click());

            ViewInteraction linearLayout5 = onView(
                    allOf(withId(R.id.relativeLayout1),
                            childAtPosition(
                                    withId(R.id.salesGridLayout),
                                    0),
                            isDisplayed()));
            linearLayout5.perform(click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction recyclerView2 = onView(
                    allOf(withId(R.id.categoriesRecyclerView),
                            withParent(allOf(withId(R.id.categoriesInnerWrapLayout),
                                    withParent(withId(R.id.categoriesWrapLayout)))),
                            isDisplayed()));
            recyclerView2.perform(actionOnItemAtPosition(1, click()));

            ViewInteraction imageView = onView(
                    allOf(withId(R.id.gridViewImage),
                            withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                    childAtPosition(
                                            withId(R.id.catalogListview),
                                            0))),
                            isDisplayed()));
            imageView.perform(click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction imageView2 = onView(
                    allOf(withId(R.id.gridViewImage),
                            withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                    childAtPosition(
                                            withId(R.id.catalogListview),
                                            1))),
                            isDisplayed()));
            imageView2.perform(click());

            ViewInteraction imageView3 = onView(
                    allOf(withId(R.id.gridViewImage),
                            withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                    childAtPosition(
                                            withId(R.id.catalogListview),
                                            2))),
                            isDisplayed()));
            imageView3.perform(click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction imageView4 = onView(
                    allOf(withId(R.id.gridViewImage),
                            withParent(allOf(withId(R.id.gridCatalogProductContainer),
                                    childAtPosition(
                                            withId(R.id.catalogListview),
                                            3))),
                            isDisplayed()));
            imageView4.perform(click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction button7 = onView(
                    allOf(withId(R.id.btnCheckOut), withText("Checkout"),
                            withParent(withId(R.id.headerTitleContainer)),
                            isDisplayed()));
            button7.perform(click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction button8 = onView(
                    allOf(withId(R.id.btnDlogRight), withText("No"), isDisplayed()));
            button8.perform(click());

            ViewInteraction button9 = onView(
                    allOf(withId(R.id.OKButton), withText("OK")));
            button9.perform(scrollTo(), click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction linearLayout6 = onView(
                    allOf(childAtPosition(
                            allOf(withId(R.id.cardsListview),
                                    withParent(withId(R.id.parentLayout))),
                            1),
                            isDisplayed()));
            linearLayout6.perform(click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ViewInteraction button10 = onView(
                    allOf(withId(R.id.exactAmountBut), withText("Exact"), isDisplayed()));
            button10.perform(click());

            ViewInteraction button11 = onView(
                    allOf(withId(R.id.processCashBut), withText("Process"), isDisplayed()));
            button11.perform(click());

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
//                ViewInteraction button12 = onView(
//                        allOf(withText("Ok"), isDisplayed()));
//                button12.perform(click());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ViewInteraction textView4 = onView(
                allOf(withText("Sync"), isDisplayed()));
        textView4.perform(click());

        ViewInteraction textView5 = onView(
                allOf(withText("Sales"), isDisplayed()));
        textView5.perform(click());

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

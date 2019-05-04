package com.smiskol.phantom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ControlsFragment extends Fragment {
    CustomSeekBar steerSeekBar;
    TextView steerTextView;
    LinearLayout holdButton;
    ImageButton speedPlusButton;
    ImageButton speedSubButton;
    TextView speedTextView;

    private OnFragmentInteractionListener mListener;

    public ControlsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controls, container, false);
        steerSeekBar = view.findViewById(R.id.steerSeekBarNew);
        steerTextView = view.findViewById(R.id.steerTextViewNew);
        speedPlusButton = view.findViewById(R.id.speedPlusButton);
        speedSubButton = view.findViewById(R.id.speedSubButton);
        holdButton = view.findViewById(R.id.holdButton);
        speedTextView = view.findViewById(R.id.speedTextView);
        setUpListeners();
        setUpHoldButton();
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setUpHoldButton() {
        holdButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    System.out.println("move button down");
                    TransitionDrawable transition = (TransitionDrawable) holdButton.getBackground();
                    transition.startTransition(175);
                    ((MainActivity) getActivity()).goDown = System.currentTimeMillis();
                    ((MainActivity) getActivity()).holdMessage = true;
                    ((MainActivity) getActivity()).buttonHeld = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    System.out.println("move button up");
                    TransitionDrawable transition = (TransitionDrawable) holdButton.getBackground();
                    transition.reverseTransition(175);
                    ((MainActivity) getActivity()).holdMessage = false;
                    ((MainActivity) getActivity()).buttonHeld = false;
                    ((MainActivity) getActivity()).goDuration = System.currentTimeMillis() - ((MainActivity) getActivity()).goDown;
                    if (((MainActivity) getActivity()).goDuration < 200) {
                        if (!(event.getAction() == MotionEvent.ACTION_CANCEL)) {
                            makeSnackbar("You must hold button down for acceleration!");
                        }
                    }
                    System.out.println("Button held for " + ((MainActivity) getActivity()).goDuration + " ms");
                }
                return false;
            }
        });
    }

    public void setUpListeners() {
        steerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                steerTextView.setText(-(progress - 100) + "°");
                ((MainActivity) getActivity()).steeringAngle = -(progress - 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ((MainActivity) getActivity()).trackingSteer = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((MainActivity) getActivity()).trackingSteer = false;
                if (seekBar.getProgress() > 100) {
                    final Integer endProgress = (steerSeekBar.getProgress() - 100) / 2;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int l = 0; l <= endProgress; l++) {
                                steerSeekBar.setProgress(steerSeekBar.getProgress() - 2);
                                try {
                                    Thread.sleep(2);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            steerSeekBar.setProgress(100);
                            ((MainActivity) getActivity()).steerLetGo = true;
                        }
                    }).start();
                } else if (seekBar.getProgress() < 100) {
                    final Integer endProgress = (100 - steerSeekBar.getProgress()) / 2;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int l = 0; l <= endProgress; l++) {
                                steerSeekBar.setProgress(steerSeekBar.getProgress() + 2);
                                try {
                                    Thread.sleep(2);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            steerSeekBar.setProgress(100);
                            ((MainActivity) getActivity()).steerLetGo = true;
                        }
                    }).start();
                }
            }
        });
        speedPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) getActivity()).sshClass.sendPhantomNew(((MainActivity) getActivity()).eonSession);
                    }
                }).start();

                ((MainActivity) getActivity()).desiredSpeed = Math.min(((MainActivity) getActivity()).desiredSpeed + 2.0, 16);
                speedTextView.setText(String.valueOf(((MainActivity) getActivity()).desiredSpeed) + " mph");
            }
        });
        speedSubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).desiredSpeed = Math.max(((MainActivity) getActivity()).desiredSpeed - 2.0, 0);
                speedTextView.setText(String.valueOf(((MainActivity) getActivity()).desiredSpeed) + " mph");
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void makeSnackbar(String s) {
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), s, Snackbar.LENGTH_SHORT);
        TextView tv = (snackbar.getView()).findViewById(android.support.design.R.id.snackbar_text);
        Typeface font = ResourcesCompat.getFont(getActivity(), R.font.product_regular);
        tv.setTypeface(font);
        snackbar.show();
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

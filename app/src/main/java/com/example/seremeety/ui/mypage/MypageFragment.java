package com.example.seremeety.ui.mypage;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.seremeety.MainActivity;
import com.example.seremeety.R;
import com.example.seremeety.databinding.FragmentMypageBinding;
import com.example.seremeety.ui.detail_profile.DetailProfileActivity;
import com.example.seremeety.ui.shop.ShopActivity;
import com.example.seremeety.utils.DialogUtils;
import com.example.seremeety.utils.KeyboardVisibilityUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MypageFragment extends Fragment {
    private FragmentMypageBinding binding;
    private MypageViewModel mypageViewModel;
    private Observer<Map<String, Object>> userDataObserver;
    private KeyboardVisibilityUtils keyboardVisibilityUtils;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri imageUri;
    private EditText birthdate;
    private EditText age;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mypageViewModel = new ViewModelProvider(this).get(MypageViewModel.class);

        binding = FragmentMypageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // userData에 대한 옵저버(fetchUserData를 통해 최신 정보를 가져오면 userData가 변함에 따라 upDateUI 호출) 등록
        userDataObserver = userData -> {
            if (userData != null) {
                updateUI(userData);
            }
        };
        mypageViewModel.getUserData().observe(getViewLifecycleOwner(), userDataObserver);

        birthdate = binding.birthdate;
        age = binding.age;

        // 현재 로그인한 사용자 정보를 가져옴
        mypageViewModel.fetchUserData();

        // ActivityResultLauncher 초기화
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                imageUri = result.getData().getData();
                Glide.with(requireContext()).load(imageUri).into(binding.profilePicture);
            }
        });

        // 프로필 사진 선택
        binding.profilePicture.setOnClickListener(v -> openFileChooser());

        // 생년월일 선택
        birthdate.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 현재 포커스를 가진 뷰의 윈도우 토큰을 사용하여 키보드를 숨김
                InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    final Calendar cldr = Calendar.getInstance();
                    int day = cldr.get(Calendar.DAY_OF_MONTH);
                    int month = cldr.get(Calendar.MONTH);
                    int year = cldr.get(Calendar.YEAR);

                    DatePickerDialog datepickerdialog = new DatePickerDialog(requireContext(), R.style.DatePickerDialogStyle,
                            new DatePickerDialog.OnDateSetListener() {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                    birthdate.setText(String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));

                                    // 만 나이 계산
                                    int currentYear = cldr.get(Calendar.YEAR);
                                    int currentMonth = cldr.get(Calendar.MONTH);
                                    int currentDay = cldr.get(Calendar.DAY_OF_MONTH);
                                    int ageYears = currentYear - year;
                                    if (currentMonth < monthOfYear || (currentMonth == monthOfYear && currentDay < dayOfMonth)) {
                                        ageYears--;
                                    }
                                    if (ageYears < 0) {
                                        age.setText("NULL");
                                    } else {
                                        age.setText(String.format("%d세", ageYears));
                                    }
                                }
                            }, year, month, day);
                    datepickerdialog.show();
                }
                return true;
            }
        });

        // MBTI 스피너 설정
        Spinner spinner = binding.spinnerMbti;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.mbti_types, R.layout.spinner_item_text);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);

        keyboardVisibilityUtils = new KeyboardVisibilityUtils(getActivity().getWindow(),
                keyboardHeight -> {
                    // 키보드가 나타날 때
                    ScrollView svRoot = binding.svMypage;
                    svRoot.smoothScrollTo(svRoot.getScrollX(), svRoot.getScrollY() + keyboardHeight);
                },
                () -> {
                    // 키보드가 숨겨질 때
                }
        );

        // 프로필 저장
        binding.saveProfile.setOnClickListener(v -> saveUserProfile());

        // 상점 이동
        binding.textNote.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ShopActivity.class);
            startActivity(intent);
        });

        // 로그아웃
        binding.signOut.setOnClickListener(view -> {
            DialogUtils.showDialog(requireContext(), "로그아웃", "로그아웃 할까요?",
                    // 확인 버튼 클릭
                    (dialog, which) -> {
                        mypageViewModel.doSignOut();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent); // MainActivity로 이동
                    },
                    // 취소 버튼 클릭
                    null);
        });

        return root;
    }

    private void updateUI(Map<String, Object> userData) {
        String profilePictureUrl = String.valueOf(userData.get("profilePictureUrl"));
        if (!TextUtils.isEmpty(profilePictureUrl)) {
            Glide.with(requireContext())
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.img_default_profile)
                    .error(R.drawable.img_default_profile)
                    .into(binding.profilePicture);
        } else {
            // 프로필 사진이 없을 경우 기본 이미지 설정
            binding.profilePicture.setImageResource(R.drawable.img_default_profile);
        }
        binding.nickname.setText(String.valueOf(userData.get("nickname")));
        binding.birthdate.setText(String.valueOf(userData.get("birthdate")));
        binding.age.setText(String.valueOf(userData.get("age")));
        String gender = String.valueOf(userData.get("gender"));
        if (gender.equals("male")) {
            binding.male.setChecked(true);
        } else if (gender.equals("female")) {
            binding.female.setChecked(true);
        }
        String mbti = String.valueOf(userData.get("mbti"));
        if (!TextUtils.isEmpty(mbti)) {
            binding.spinnerMbti.setSelection(getMbtiIndex(mbti));
        }
        binding.university.setText(String.valueOf(userData.get("university")));
        binding.place.setText(String.valueOf(userData.get("place")));
        binding.introduce.setText(String.valueOf(userData.get("introduce")));
        binding.coin.setText(String.valueOf(userData.get("coin")));

        // profileStatus가 0인 경우(프로필을 최초 1회 수정 시)에만 생년월일, 성별 수정 가능
        int profileStatus = Integer.parseInt(String.valueOf(userData.get("profileStatus")));
        boolean isEditable = profileStatus == 0;
        binding.birthdate.setEnabled(isEditable);
        binding.male.setEnabled(isEditable);
        binding.female.setEnabled(isEditable);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(intent);
    }

    private int getMbtiIndex(String mbti) {
        String[] mbtiArray = getResources().getStringArray(R.array.mbti_types);
        for (int i = 0; i < mbtiArray.length; i++) {
            if (mbtiArray[i].equals(mbti)) {
                return i;
            }
        }
        return 0;
    }

    private void saveUserProfile() {
        String updatedNickname = binding.nickname.getText().toString();
        String updatedBirthdate = binding.birthdate.getText().toString();
        String updatedAge = binding.age.getText().toString();
        String updatedMbti = binding.spinnerMbti.getSelectedItem().toString();
        String updatedUniversity = binding.university.getText().toString();
        String updatedPlace = binding.place.getText().toString();
        String updatedIntroduce = binding.introduce.getText().toString();

        String updatedGender = "";
        int selectedGenderId = binding.gender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.male) {
            updatedGender = "male";
        } else if (selectedGenderId == R.id.female) {
            updatedGender = "female";
        }

        Map<String, Object> updatedUserData = new HashMap<>();
        updatedUserData.put("nickname", updatedNickname);
        updatedUserData.put("birthdate", updatedBirthdate);
        updatedUserData.put("age", updatedAge);
        updatedUserData.put("mbti", updatedMbti);
        updatedUserData.put("university", updatedUniversity);
        updatedUserData.put("place", updatedPlace);
        updatedUserData.put("introduce", updatedIntroduce);
        updatedUserData.put("gender", updatedGender);

        // 프로필 상태를 1로 설정
        updatedUserData.put("profileStatus", 1);

        if (imageUri != null) {
            mypageViewModel.uploadProfilePicture(imageUri, uri -> {
                String profilePictureUrl = uri.toString();
                updatedUserData.put("profilePictureUrl", profilePictureUrl);

                // 프로필 사진 URL이 성공적으로 저장된 후에 업데이트
                mypageViewModel.updateUserData(updatedUserData, success -> {
                    if (success) {
                        DialogUtils.showConfirmationDialog(requireContext(), "프로필 저장 성공", "성공적으로 저장되었어요!");
                    } else {
                        DialogUtils.showConfirmationDialog(requireContext(), "프로필 저장 실패", "다시 시도해주세요");
                    }
                });
            });
        } else {
            // 프로필 사진이 없는 경우에도 업데이트
            mypageViewModel.updateUserData(updatedUserData, success -> {
                if (success) {
                    DialogUtils.showConfirmationDialog(requireContext(), "프로필 저장 성공", "성공적으로 저장되었어요!");
                } else {
                    DialogUtils.showConfirmationDialog(requireContext(), "프로필 저장 실패", "다시 시도해주세요");
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (keyboardVisibilityUtils != null) {
            keyboardVisibilityUtils.detachKeyboardListeners();
        }
        binding = null;
        if (userDataObserver != null) {
            mypageViewModel.getUserData().removeObserver(userDataObserver);
            Log.d(TAG, "destroy observer");
        }
        Log.d(TAG, "onDestroyView");
    }
}




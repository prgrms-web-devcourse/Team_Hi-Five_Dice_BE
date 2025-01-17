package com.cocodan.triplan.member.service;

import com.cocodan.triplan.exception.common.UniqueEmailException;
import com.cocodan.triplan.member.dto.response.*;
import com.cocodan.triplan.util.MemberConverter;
import com.cocodan.triplan.member.domain.Member;
import com.cocodan.triplan.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
@Slf4j
public class MemberService {

    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    private final MemberConverter converter;

    public MemberService(MemberRepository memberRepository, MemberConverter converter) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.memberRepository = memberRepository;
        this.converter = converter;
    }

    @Transactional
    public MemberCreateResponse create(String email, String name, String birth, String gender, String nickname, String profileImage, String passwd, Long groupId) {
        Member originMember = converter.toMemberEntity(email, name, birth, gender, nickname, profileImage, passwordEncoder.encode(passwd), groupId);
        Member memberEntity = memberRepository.save(originMember);

        return converter.toMemberCreateResponse(memberEntity);
    }

    @Transactional
    public MemberCreateResponse validCreate(String email, String name, String birth, String gender, String nickname, String profileImage, String passwd, Long groupId) {
        if(memberRepository.findByEmail(email).isPresent())
            throw new UniqueEmailException(MemberService.class, email);

        Member originMember = converter.toMemberEntity(email, name, birth, gender, nickname, profileImage, passwordEncoder.encode(passwd), groupId);
        Member memberEntity = memberRepository.save(originMember);

        return converter.toMemberCreateResponse(memberEntity);
    }

    @Transactional(readOnly = true)
    public MemberGetOneResponse getOne(Long id) {
        return memberRepository.findById(id)
                .map(converter::toMemberFindResponse)
                .orElseThrow(
                        () -> new RuntimeException("사용자 id를 조회할 수 없습니다")
                );
    }

    @Transactional(readOnly = true)
    public List<MemberSimpleResponse> findMemberByNickname(String nickname){
        return memberRepository.findAllByNicknameContaining(nickname).stream()
                .map(MemberSimpleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberUpdateResponse update(Long id, String name, String nickname, String profileImage) {
        Member originMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자 id를 조회할 수 없습니다"));

        originMember.changeValues(name, nickname, profileImage);
        return converter.toMemberUpdateResponse(originMember);
    }

    @Transactional
    public MemberDeleteResponse delete(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("사용자 id를 조회할 수 없습니다")
                );

        MemberDeleteResponse result = converter.toMemberDeleteResponse(member);
        memberRepository.delete(member);
        return result;
    }

    @Transactional(readOnly = true)
    public Member login(String email, String credentials) {
        checkArgument(isNotEmpty(email), "principal must be provided.");
        checkArgument(isNotEmpty(credentials), "credentials must be provided.");

        Member member = memberRepository.findByLoginId(email)
                .orElseThrow(() -> new UsernameNotFoundException("Could not found user for " + email));

        if (!passwordEncoder.matches(credentials, member.getPassword()))
        {
            log.warn("Invalid password. user email : " + email);
            throw new IllegalArgumentException("Invalid password.");
        }

        return member;
    }
}

-- 사이드바 순서. 켜짐/꺼짐과 같은 "내 모듈 구성"이라 같은 행에 둔다.
-- 기본값을 크게 두어, 순서를 정한 적 없는 모듈이 뒤로 가게 한다.
alter table module_enrollments
    add column sort_order integer not null default 1000;

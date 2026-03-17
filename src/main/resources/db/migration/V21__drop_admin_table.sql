-- Drop legacy admin table (adminlogin module removal)
-- NOTE: Admin auth is unified under accounts/admin_profiles.
-- 판단: adminlogin 모듈 제거 이후 관리자 인증 로직은 accounts/admin_profiles에서 처리되므로 admin 테이블은 필요 없음
-- 참고: adminlogin 모듈은 2026년 3월 17일 이후 제거되었습니다.
-- adminlogin 모듈의 제거 이유: adminlogin 모듈은 관리자 인증 로직을 처리하는 모듈이었습니다. 
-- 하지만 관리자 인증 로직은 accounts/admin_profiles에서 처리되므로 adminlogin 모듈이 사용되지 않았음을 확인하였습니다.
-- 어플 내에서도 adminlogin 모듈 사용 없었으며, adminlogin 모듈을 참조하는 관련 코드가 없었음을 확인하여 진행을 하게 되었습니다.

DROP TABLE IF EXISTS admin;


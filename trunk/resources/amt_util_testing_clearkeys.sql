---This is just a utility I used a lot while testing the AMT keys.
---Don't run it on a produciton server, since it will wipe everything AMT-related

delete from user where amt_visitor='1';

delete from `amtkey`;

insert into `amtkey` (`key`, `expiration`, `created`) values ('justtesting', '72036854775807', '19830905132800');
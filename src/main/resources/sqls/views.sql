/*vw_repository*/
CREATE VIEW vw_repository AS
SELECT p.id,
       p.name,
       p.full_name,
       p.tags,
       p.create_time,
       p.user_id,
       p.source_url,
       p.build_script,
       p.deploy_server,
       p.service_url,
       p.owner_name,
       p.owner_kind,
       p.summary,
       p.status,
       p.last_message,
       p.is_public,
       p.member_count,
       dpm.user_id AS my_id,
       dpm.permission,
       dpm.owner
FROM dev_repository_member dpm
         LEFT JOIN dev_repository p ON p.id::text = dpm.repository_id::text

/*vw_repository_member*/
CREATE VIEW vw_repository_member AS
SELECT dev_repository_member.user_id,
       dev_repository_member.repository_id,
       dev_repository_member.permission,
       dev_repository_member.create_time,
       dev_repository_member.owner,
       rbac_user.user_name,
       rbac_user.nick_name,
       rbac_user.user_type,
       rbac_user.email,
       rbac_user.avatar,
       rbac_user.status
FROM dev_repository_member
         INNER JOIN
     rbac_user
     ON
         dev_repository_member.user_id = rbac_user.user_id